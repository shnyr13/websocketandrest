package padev.websocketandrest

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class MyX509TrustManager: X509TrustManager{
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        throw CertificateException()
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        throw CertificateException()
    }

    override fun getAcceptedIssuers(): Array<X509Certificate>? {
        return arrayOf()
    }
}