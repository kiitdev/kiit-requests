package kiit.requests

/**
 * String content with type/format information attached. Lets a handler return this instead of
 * a plain string when the caller (an HTTP server, say) needs to know the content type to send
 * back, without every handler having to know about content types itself.
 */
interface Content {
    val data: ByteArray
    val tpe: ContentType

    val isEmpty: Boolean get() = data.isEmpty()

    val isDefined: Boolean get() = !isEmpty

    val size: Int get() = data.size
}

class ContentText(override val data: ByteArray, val raw: String, override val tpe: ContentType) : Content

class ContentData(override val data: ByteArray, val raw: String?, override val tpe: ContentType) : Content

/**
 * A named file's content, e.g. a multipart upload on the server side or an attachment staged
 * for an outbound call on the client side.
 */
class ContentFile(val name: String, override val data: ByteArray, val raw: String?, override val tpe: ContentType) :
    Content

data class ContentMulti(val fields: Map<String, ContentText>, val files: Map<String, ContentFile>)

object Contents {
    fun csv(text: String): Content = ContentText(text.encodeToByteArray(), text, ContentTypes.Csv)

    fun html(text: String): Content = ContentText(text.encodeToByteArray(), text, ContentTypes.Html)

    fun json(text: String): Content = ContentText(text.encodeToByteArray(), text, ContentTypes.Json)

    fun text(text: String): Content = ContentText(text.encodeToByteArray(), text, ContentTypes.Plain)

    fun xml(text: String): Content = ContentText(text.encodeToByteArray(), text, ContentTypes.Xml)

    fun other(text: String, tpe: ContentType): Content = ContentText(text.encodeToByteArray(), text, tpe)

    fun toText(content: Content?): String? {
        return when (content) {
            null -> ""
            is ContentText -> content.raw
            is ContentData -> content.raw ?: content.data.decodeToString()
            else -> content.data.decodeToString()
        }
    }
}
