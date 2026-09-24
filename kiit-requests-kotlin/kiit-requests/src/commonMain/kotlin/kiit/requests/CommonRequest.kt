@file:OptIn(ExperimentalUuidApi::class)

package kiit.requests

import kiit.context.Identity
import kiit.context.Source
import kiit.inputs.Inputs
import kiit.inputs.ListMap
import kiit.inputs.Meta
import kiit.inputs.RecordMap
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.jvm.JvmStatic
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A [Meta], delegating the read side to a [RecordMap] and adding [toMap]/[getAll] on top.
 * Backed by a plain, single-value-per-key `Map`, so [getAll] only ever returns zero or one
 * value; a caller that needs true multi-value meta (repeated headers) should build a
 * `kiit.inputs.MetaMap` directly instead of going through [CommonRequest]'s factories.
 */
private class RequestMeta(private val fields: Map<String, Any?>) :
    Meta, Inputs by RecordMap(ListMap(fields.toList())) {
    override fun toMap(): Map<String, Any> = fields.filterValues { it != null }.mapValues { it.value as Any }

    override fun getAll(key: String): List<String> = fields[key]?.let { listOf(it.toString()) } ?: emptyList()
}

/**
 * Default implementation of [Request].
 */
data class CommonRequest(
    override val path: String,
    override val parts: List<String>,
    override val source: Source,
    override val verb: Verb,
    override val data: Inputs,
    override val args: Inputs,
    override val params: Inputs,
    override val meta: Meta,
    override val callerId: Identity,
    override val raw: Any? = null,
    override val output: String? = null,
    override val tag: List<String> = listOf(),
    override val version: Version = Version(api = "0"),
    override val requestId: String = Uuid.random().toString(),
    override val files: Files = Files.None,
    override val trace: Trace? = null,
    override val timestamp: Instant = Clock.System.now(),
) : Request {
    override fun clone(
        path: String,
        parts: List<String>,
        source: Source,
        verb: Verb,
        data: Inputs,
        args: Inputs,
        params: Inputs,
        meta: Meta,
        raw: Any?,
        output: String?,
        tag: List<String>,
        version: Version,
        requestId: String,
        callerId: Identity,
        files: Files,
        trace: Trace?,
        timestamp: Instant,
    ): Request {
        return this.copy(
            path = path,
            parts = parts,
            source = source,
            verb = verb,
            data = data,
            args = args,
            params = params,
            meta = meta,
            raw = raw,
            output = output,
            tag = tag,
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

        private fun metadata(map: Map<String, Any>): Meta = RequestMeta(map)

        /**
         * Builds an API/HTTP-style request. `data` is the body/payload map; `meta` is
         * header-like settings. `args`/`params` start empty. A host resolving query-string
         * args or path params populates those directly rather than going through this factory.
         */
        @JvmStatic
        fun api(
            area: String,
            name: String,
            action: String,
            verb: Verb,
            callerId: Identity,
            meta: Map<String, Any> = mapOf(),
            data: Map<String, Any> = mapOf(),
            raw: Any? = null,
        ): Request {
            val path = if (area.isEmpty()) "$name.$action" else "$area.$name.$action"
            return CommonRequest(
                path = path,
                parts = listOf(area, name, action),
                source = Source.API,
                verb = verb,
                data = inputs(data),
                args = inputs(mapOf()),
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
            name: String,
            action: String,
            verb: Verb,
            callerId: Identity,
            meta: Map<String, Any> = mapOf(),
            data: Map<String, Any> = mapOf(),
            raw: Any? = null,
            version: Version = Version(api = "0"),
        ): Request {
            val path = if (area.isEmpty()) "$name.$action" else "$area.$name.$action"
            return CommonRequest(
                path = path,
                parts = listOf(area, name, action),
                source = Source.CLI,
                verb = verb,
                data = inputs(data),
                args = inputs(mapOf()),
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
            meta: Map<String, Any> = mapOf(),
            data: Map<String, Any> = mapOf(),
            raw: Any? = null,
            version: Version = Version(api = "0"),
        ): Request {
            val parts = path.split(".")
            return cli(
                area = parts.getOrElse(0) { "" },
                name = parts.getOrElse(1) { "" },
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
