@file:OptIn(ExperimentalUuidApi::class)

package kiit.requests

import kiit.call.Identity
import kiit.inputs.Args
import kiit.inputs.ArgsMap
import kiit.inputs.Inputs
import kiit.inputs.ListMap
import kiit.inputs.Meta
import kiit.inputs.MetaMap
import kiit.inputs.RecordMap
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.jvm.JvmStatic
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Kiit's own real, ready-to-use [ServerRequest]: combines it with [KiitRouting], the
 * `area`/`api`/`action` convention kiit-cli/kiit-tasks/kiit-apis dispatch on. Anything that
 * needs that convention takes/builds a `KiitRequest`, not a plain `ServerRequest`, so the
 * convention stays opt-in at the type level rather than assumed on every server request.
 */
data class KiitRequest(
    override val path: String,
    override val parts: List<String>,
    override val source: Source,
    override val verb: Verb,
    override val data: Inputs,
    override val args: Args,
    override val params: Inputs,
    override val meta: Meta,
    override val callerId: Identity,
    override val raw: Any? = null,
    override val format: ContentType = ContentTypes.Json,
    override val tags: List<Tag> = listOf(),
    override val version: Version = Version(api = "0"),
    override val requestId: String = Uuid.random().toString(),
    override val files: Files = Files.None,
    override val trace: Trace? = null,
    override val timestamp: Instant = Clock.System.now(),
) : ServerRequest, KiitRouting {
    override fun structured(): List<Pair<String, Any?>> {
        return super.structured() +
            listOf(
                KiitRouting::area.name to area,
                KiitRouting::api.name to api,
                KiitRouting::action.name to action,
            )
    }

    override fun clone(
        path: String,
        source: Source,
        verb: Verb,
        data: Inputs,
        args: Args,
        params: Inputs,
        meta: Meta,
        raw: Any?,
        format: ContentType,
        tags: List<Tag>,
        version: Version,
        requestId: String,
        callerId: Identity,
        files: Files,
        trace: Trace?,
        timestamp: Instant,
    ): KiitRequest {
        return this.copy(
            path = path,
            source = source,
            verb = verb,
            data = data,
            args = args,
            params = params,
            meta = meta,
            raw = raw,
            format = format,
            tags = tags,
            version = version,
            requestId = requestId,
            callerId = callerId,
            files = files,
            trace = trace,
            timestamp = timestamp,
        )
    }

    companion object {
        private fun inputs(map: Map<String, Any>): Inputs = RecordMap(ListMap(map.toList()))

        private fun metadata(map: Map<String, String>): Meta = MetaMap(ListMap(map.toList()))

        /**
         * Builds an API/HTTP-style request. `data` is the body/payload map; `meta` is
         * header-like settings. `args`/`params` start empty. A host resolving query-string
         * args or path params populates those directly rather than going through this factory.
         */
        @JvmStatic
        fun api(
            area: String,
            api: String,
            action: String,
            verb: Verb,
            callerId: Identity,
            meta: Map<String, String> = mapOf(),
            data: Map<String, Any> = mapOf(),
            raw: Any? = null,
        ): KiitRequest {
            val path = if (area.isEmpty()) "$api.$action" else "$area.$api.$action"
            return KiitRequest(
                path = path,
                parts = listOf(area, api, action),
                source = Source.Api,
                verb = verb,
                data = inputs(data),
                args = ArgsMap(ListMap()),
                params = inputs(mapOf()),
                meta = metadata(meta),
                callerId = callerId,
                raw = raw,
            )
        }

        /** Builds a CLI-style request using the raw data/meta supplied. */
        @JvmStatic
        fun cli(
            area: String,
            api: String,
            action: String,
            verb: Verb,
            callerId: Identity,
            meta: Map<String, String> = mapOf(),
            data: Map<String, Any> = mapOf(),
            raw: Any? = null,
            version: Version = Version(api = "0"),
        ): KiitRequest {
            val path = if (area.isEmpty()) "$api.$action" else "$area.$api.$action"
            return KiitRequest(
                path = path,
                parts = listOf(area, api, action),
                source = Source.Cli,
                verb = verb,
                data = inputs(data),
                args = ArgsMap(ListMap()),
                params = inputs(mapOf()),
                meta = metadata(meta),
                callerId = callerId,
                raw = raw,
                version = version,
            )
        }

        /** Builds a request from a dot-delimited path, e.g. `"app.users.activate"`. */
        @JvmStatic
        fun path(
            path: String,
            verb: Verb,
            callerId: Identity,
            meta: Map<String, String> = mapOf(),
            data: Map<String, Any> = mapOf(),
            raw: Any? = null,
            version: Version = Version(api = "0"),
        ): KiitRequest {
            val parts = path.split(".")
            return cli(
                area = parts.getOrElse(0) { "" },
                api = parts.getOrElse(1) { "" },
                action = parts.getOrElse(2) { "" },
                verb = verb,
                callerId = callerId,
                meta = meta,
                data = data,
                raw = raw,
                version = version,
            )
        }
    }
}
