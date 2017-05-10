package modules.tools

/**
  * Object FNV implements FNV-1 and FNV-1a, non-cryptographic hash functions created by Glenn Fowler, Landon Curt Noll, and Phong Vo.
  * See http://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function.
  */
object FNV {

  private val INIT32  = BigInt("811c9dc5", 16)
  private val INIT64  = BigInt("cbf29ce484222325", 16)
  private val PRIME32 = BigInt("01000193", 16)
  private val PRIME64 = BigInt("100000001b3", 16)
  private val MOD32   = BigInt("2").pow(32)
  private val MOD64   = BigInt("2").pow(64)
  private val MASK    = 0xff

  @inline private final def calc(prime: BigInt, mod: BigInt)(hash: BigInt, b: Byte): BigInt =
    ((hash * prime) % mod) ^ (b & MASK)
  @inline private final def calcA(prime: BigInt, mod: BigInt)(hash: BigInt, b: Byte): BigInt =
    ((hash ^ (b & MASK)) * prime) % mod

  /**
    * Calculates 32bit FNV-1 hash
    * @param data the data to be hashed
    * @return a 32bit hash value
    */
  @inline final def hash32(data: Array[Byte]): BigInt = data.foldLeft(INIT32)(calc(PRIME32, MOD32))

  /**
    * Calculates 32bit FNV-1a hash
    * @param data the data to be hashed
    * @return a 32bit hash value
    */
  @inline final def hash32a(data: Array[Byte]): BigInt = data.foldLeft(INIT32)(calcA(PRIME32, MOD32))

  /**
    * Calculates 64bit FNV-1 hash
    * @param data the data to be hashed
    * @return a 64bit hash value
    */
  @inline final def hash64(data: Array[Byte]): BigInt = data.foldLeft(INIT64)(calc(PRIME64, MOD64))

  /**
    * Calculates 64bit FNV-1a hash
    * @param data the data to be hashed
    * @return a 64bit hash value
    */
  @inline final def hash64a(data: Array[Byte]): BigInt = data.foldLeft(INIT64)(calcA(PRIME64, MOD64))
}
