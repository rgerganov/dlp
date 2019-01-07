Programs solving the discrete logarithm problem.

Problem statement: find value `a` such that `g^a mod p = A`

`a` is 31 bit unsigned integer; `g`, `p` and `A` are specified in a .conf file, in hex format like this:

    g=A4D1CBD5C3FD34126765A442EFB99905F8104DD258AC507FD6406CFF14266D31266FEA1E5C41564B777E690F5504F213160217B4B01B886A5E91547F9E2749F4D7FBD7D3B9A92EE1909D0D2263F80A76A6A24C087A091F531DBF0A0169B6A28AD662A4D18E73AFA32D779D5918D08BC8858F4DCEF97C2A24855E6EEB22B3B2E5
    p=B10B8F96A080E01DDE92DE5EAE5D54EC52C99FBCFB06A3C69A6A9DCA52D23B616073E28675A23D189838EF1E2EE652C013ECB4AEA906112324975C3CD49B83BFACCBDD7D90C4BD7098488E9C219A73724EFFD6FAE5644738FAA31A4FF55BCCC0A151AF5F0DC8B4BD45BF37DF365C1A65E68CFDA76D4DA708DF1FB2BC2E4A4371
    A=4FB7FC5543219711B7144FDA72E4A25DDCBC79DB02D51C742602FB3D0D40E04C46CD22EC33B43DBEB5C05217A9135904DD8B7915335C9337D6CF07464E6E4D762B2C8B3A2F84313D0014C74D4EFE1FB00147B3D8498A755D6E2E6729A13B0F086BFEAB83E37B6401FEA9884AC1E493D7F91A065CD25E22EE5A66433F8C308DED
    start=0
    threads=8

The last two options specify the start value and the number of threads being used.

There is Java and C implementation. The C version is using the `crypto` library and is 2 times faster than the Java version which uses the built-in `BigInteger` class.

Build
---

    ./build.sh
    
Run
---
     java -cp bin dlp.DLP dlp.conf
 
or:

    bin/dlpc dlp.conf
