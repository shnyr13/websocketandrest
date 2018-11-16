package padev.websocketandrest

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

class MyHostnameVerifier: HostnameVerifier {
    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        val hv = HttpsURLConnection.getDefaultHostnameVerifier()
        return hv.verify("insight.bitpay.com", session)
    }
}