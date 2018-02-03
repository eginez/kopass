import com.eginez.kopass.readPublicKeyFromKeyRing
import org.junit.Test
import java.io.*

class CryptoTest {
    @Test
    fun testSimple() {
        val ins = FileInputStream("/Users/eginez/.gnupg/pubring.gpg")
        val key = readPublicKeyFromKeyRing(ins, "A093898C")
        assert(key != null)
    }
}