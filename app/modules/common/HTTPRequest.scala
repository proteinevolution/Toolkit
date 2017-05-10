package modules.common

/**
  * Created by zin on 16.08.16.
  */
import play.api.http.HeaderNames
import play.api.mvc.RequestHeader
import scalaz._
import Scalaz._

final case class HTTPRequest(req: RequestHeader) {

  /**
    * Regex Matchers
    */
  val isAndroid = UaMatcher("""(?i).*android.+mobile.*""".r)
  val isIOS     = UaMatcher("""(?i).*(iphone|ipad|ipod).*""".r)
  val isMobile  = UaMatcher("""(?i).*(iphone|ipad|ipod|android.+mobile).*""".r)
  val isBot = UaMatcher {
    ("""(?i).*(googlebot|googlebot-mobile|googlebot-image|mediapartners-google|bingbot|slurp|java|wget|curl|commons-httpclient|python-urllib|libwww|httpunit|nutch|phpcrawl|msnbot|adidxbot|blekkobot|teoma|ia_archiver|gingercrawler|webmon|httrack|webcrawler|fast-webcrawler|fastenterprisecrawler|convera|biglotron|grub\.org|usinenouvellecrawler|antibot|netresearchserver|speedy|fluffy|jyxobot|bibnum\.bnf|findlink|exabot|gigabot|msrbot|seekbot|ngbot|panscient|yacybot|aisearchbot|ioi|ips-agent|tagoobot|mj12bot|dotbot|woriobot|yanga|buzzbot|mlbot|purebot|lingueebot|yandex\.com/bots|""" +
      """voyager|cyberpatrol|voilabot|baiduspider|citeseerxbot|spbot|twengabot|postrank|turnitinbot|scribdbot|page2rss|sitebot|linkdex|ezooms|dotbot|mail\.ru|discobot|zombie\.js|heritrix|findthatfile|europarchive\.org|nerdbynature\.bot|sistrixcrawler|ahrefsbot|aboundex|domaincrawler|wbsearchbot|summify|ccbot|edisterbot|seznambot|ec2linkfinder|gslfbot|aihitbot|intelium_bot|yeti|retrevopageanalyzer|lb-spider|sogou|lssbot|careerbot|wotbox|wocbot|ichiro|duckduckbot|lssrocketcrawler|drupact|webcompanycrawler|acoonbot|openindexspider|gnamgnamspider|web-archive-net\.com\.bot|backlinkcrawler|""" +
      """coccoc|integromedb|contentcrawlerspider|toplistbot|seokicks-robot|it2media-domain-crawler|ip-web-crawler\.com|siteexplorer\.info|elisabot|proximic|changedetection|blexbot|arabot|wesee:search|niki-bot|crystalsemanticsbot|rogerbot|360spider|psbot|interfaxscanbot|lipperheyseoservice|ccmetadatascaper|g00g1e\.net|grapeshotcrawler|urlappendbot|brainobot|fr-crawler|binlar|simplecrawler|simplecrawler|livelapbot|twitterbot|cxensebot|smtbot|facebookexternalhit|daumoa|sputnikimagebot|visionutils|yisouspider|parsijoobot|mediatoolkit\.com).*""").r
  }
  //val isHuman   = !isBot

  def isXhr: Boolean =
    (req.headers get "X-Requested-With") contains "XMLHttpRequest"

  def isSocket: Boolean =
    (req.headers get HeaderNames.UPGRADE.toLowerCase) == Some("websocket")

  def isSynchronousHttp =
    !isXhr && !isSocket

  def isSafe =
    req.method == "GET"

  def isRedirectable =
    isSynchronousHttp && isSafe

  def userAgent: Option[String] =
    req.headers get HeaderNames.USER_AGENT

  private def uaContains(str: String) = userAgent.exists(_ contains str)

  def isTrident = uaContains("Trident/")
  def isChrome  = uaContains("Chrome/")
  def isSafari  = uaContains("Safari/") && !isChrome

  def referer: Option[String] =
    req.headers get HeaderNames.REFERER

  def lastRemoteAddress: String =
    req.remoteAddress.split(", ").lastOption | req.remoteAddress

  def sid: Option[String] =
    req.session get "sid"

  private def UaMatcher(regex: scala.util.matching.Regex): RequestHeader => Boolean = { req =>
    {
      userAgent match {
        case `regex` => true
        case _       => false
      }
    }
  }

  def isFacebookBot =
    userAgent contains "facebookexternalhit"

  private val fileExtensionPattern = """.+\.[a-z0-9]{2,4}$""".r.pattern

  def hasFileExtension =
    fileExtensionPattern.matcher(req.path).matches
}
