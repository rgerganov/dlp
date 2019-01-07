/*
 * Solves the discrete logarithm problem.
 * Find value "a" such that g^a mod p = A
 *
 * "a" is 31 bit unsigned integer
 * "g", "p" and "A" are given (in conf file)
 */
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <openssl/bn.h>
#include <pthread.h>

#define BATCH_SIZE (1<<20)

BIGNUM *g = NULL, *p = NULL, *A = NULL;
int32_t start = -1;
int32_t threads = -1;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

void parse_conf(const char *fpath)
{
    FILE *f = fopen(fpath, "r");
    if (f == NULL) {
        fprintf(stderr, "Cannot find %s\n", fpath);
        exit(1);
    }
    char buf[2048];
    char tmp[2048];
    while (fgets(buf, sizeof(buf), f) != NULL) {
        if (strncmp(buf, "g=", 2) == 0) {
            sscanf(buf, "g=%s\n", tmp);
            printf("g=%s\n", tmp);
            BN_hex2bn(&g, tmp);
        } else if (strncmp(buf, "p=", 2) == 0) {
            sscanf(buf, "p=%s\n", tmp);
            printf("p=%s\n", tmp);
            BN_hex2bn(&p, tmp);
        } else if (strncmp(buf, "A=", 2) == 0) {
            sscanf(buf, "A=%s\n", tmp);
            printf("A=%s\n", tmp);
            BN_hex2bn(&A, tmp);
        } else if (strncmp(buf, "start=", 6) == 0) {
            sscanf(buf, "start=%d\n", &start);
        } else if (strncmp(buf, "threads=", 8) == 0) {
            sscanf(buf, "threads=%d\n", &threads);
        }
    }
    fclose(f);
    if (g == NULL || p == NULL || A == NULL || start == -1 || threads == -1) {
        fprintf(stderr, "Invalid configuration\n");
        exit(1);
    }
}

void *dlp(void *ptr)
{
    BN_CTX *ctx = BN_CTX_new();
    BIGNUM *r = BN_new();
    BIGNUM *start_bn = BN_new();
    while (1) {
        int32_t loc_start = -1;
        pthread_mutex_lock(&mutex);
        loc_start = start;
        start += BATCH_SIZE;
        pthread_mutex_unlock(&mutex);
        if (loc_start < 0) {
            break;
        }
        int32_t end = loc_start + BATCH_SIZE;
        if (end < 0) {
            end = INT32_MAX;
        }
        printf("Testing from %d to %d\n", loc_start, end);
        BN_set_word(start_bn, loc_start);
        BN_mod_exp(r, g, start_bn, p, ctx);
        for (int32_t i = loc_start ; i < end ; i++) {
            if (BN_cmp(r, A) == 0) {
                printf("solution found, a=%d\n", i);
                exit(2);
            }
            BN_mod_mul(r, r, g, p, ctx);
        }
    }
    BN_CTX_free(ctx);
    return NULL;
}

int main(int argc, char *argv[])
{
    if (argc < 2) {
        fprintf(stderr, "Usage: dlpc <conf_file>\n");
        return 1;
    }
    parse_conf(argv[1]);
    printf("Solving g^a mod p = A with %d threads\n\n", threads);
    pthread_t th[threads];
    for (int i = 0 ; i < threads ; i++) {
        if (pthread_create(&th[i], NULL, dlp, NULL)) {
            fprintf(stderr, "Error creating thread\n");
            return 1;
        }
    }
    for (int i = 0 ; i < threads ; i++) {
        pthread_join(th[i], NULL);
    }
    return 0;
}
