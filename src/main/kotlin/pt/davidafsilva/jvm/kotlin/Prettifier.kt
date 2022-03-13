package pt.davidafsilva.jvm.kotlin

import java.lang.Integer.toHexString
import java.lang.System.identityHashCode
import java.lang.System.lineSeparator
import java.time.Duration
import java.time.LocalTime
import java.time.chrono.ChronoLocalDate
import java.time.chrono.ChronoLocalDateTime
import java.time.chrono.ChronoZonedDateTime
import java.time.format.DateTimeFormatter.ISO_DATE
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.format.DateTimeFormatter.ISO_TIME
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import java.util.Collections.newSetFromMap
import java.util.Comparator.comparingInt
import java.util.IdentityHashMap
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Tries to convert the function's receiver into a prettified string.
 *
 * Currently only data classes are eligible to being prettified.
 * The output of the function when the receiver is not a data class should be roughly equivalent of directly
 * calling its [toString].
 *
 * @param indentationWidth the indentation width to be applied. Defaults to 2.
 */
fun Any?.toPrettyString(indentationWidth: Int = 2): String {
    val sb = StringBuilder()
    prettifyObject(from = this, to = sb, indentationWidth)
    return sb.toString()
}

private fun prettifyObject(
    from: Any?,
    to: StringBuilder,
    indentationWidth: Int,
    // use a set implementation that uses reference equality to ensure that it is indeed the same object
    seen: MutableSet<Any> = newSetFromMap(IdentityHashMap()),
    depth: Int = 1
) {
    when {
        // null objects
        from == null -> to.append("null")
        // avoid circular references
        !seen.add(from) -> to.append("<ref ${from.objectReference()}>")
        // skip data classes
        !from::class.isData -> to.append(from.toString())
        // data classes :)
        else -> prettifyDataClass(from, to, indentationWidth, seen, depth)
    }
}

private fun prettifyDataClass(from: Any, to: StringBuilder, indentationWidth: Int, seen: MutableSet<Any>, depth: Int) {
    // header
    val clazz = from::class
    to.append(clazz.simpleName)
        .append(from.objectReference())
        .append("(")
        .append(lineSeparator())

    // body
    val orderedMembers = clazz.primaryConstructor?.parameters.orEmpty().associate { it.name!! to it.index }
    val comparator = comparingInt<KProperty1<out Any, *>?> { member ->
        orderedMembers[member.name] ?: orderedMembers.size
    }.thenComparing(KProperty1<out Any, *>::name)
    val members = clazz.memberProperties.sortedWith(comparator)
    members.forEach { member -> prettifyMemberProperty(from, member, to, indentationWidth, seen, depth) }

    // footer
    to.append(indent(depth - 1, indentationWidth))
        .append(")")
}

private fun prettifyMemberProperty(
    obj: Any,
    member: KProperty1<out Any, *>,
    to: StringBuilder,
    indentationWidth: Int,
    seen: MutableSet<Any>,
    depth: Int
) {
    // indentation
    to.append(indent(depth, indentationWidth))

    // property name
    to.append(member.name).append(" = ")

    // property value
    val value = member.call(obj)
    prettifyValue(value, to, indentationWidth, seen, depth)
    to.append(",") // always add a trailing comma
        .append(lineSeparator())
}

private fun prettifyValue(value: Any?, to: StringBuilder, indentationWidth: Int, seen: MutableSet<Any>, depth: Int) {
    when (value) {
        // null values
        null -> to.append("null")
        // enum
        is Enum<*> -> to.append("\"${value.name}\"")
        // string / characters
        is String -> to.append("\"$value\"")
        is Char -> to.append("'$value'")
        // collection / arrays
        is Array<*> -> prettifyCollection(to, value.iterator(), indentationWidth, seen, depth)
        is CharArray -> prettifyCollection(to, value.iterator(), indentationWidth, seen, depth)
        is ByteArray -> prettifyCollection(to, value.iterator(), indentationWidth, seen, depth)
        is ShortArray -> prettifyCollection(to, value.iterator(), indentationWidth, seen, depth)
        is IntArray -> prettifyCollection(to, value.iterator(), indentationWidth, seen, depth)
        is LongArray -> prettifyCollection(to, value.iterator(), indentationWidth, seen, depth)
        is DoubleArray -> prettifyCollection(to, value.iterator(), indentationWidth, seen, depth)
        is FloatArray -> prettifyCollection(to, value.iterator(), indentationWidth, seen, depth)
        is Collection<*> -> prettifyCollection(to, value.iterator(), indentationWidth, seen, depth)
        // maps
        is Map<*, *> -> prettifyMap(to, value, indentationWidth, seen, depth)
        // raw values
        is Byte -> to.append("0x%02x".format(value))
        is Number, is Boolean -> to.append(value)
        // dates
        is ChronoLocalDate -> to.append("\"${ISO_DATE.format(value)}\"")
        is LocalTime -> to.append("\"${ISO_TIME.format(value)}\"")
        is ChronoLocalDateTime<*> -> to.append("\"${ISO_DATE_TIME.format(value)}\"")
        is ChronoZonedDateTime<*> -> to.append("\"${ISO_ZONED_DATE_TIME.format(value)}\"")
        is Duration, is kotlin.time.Duration -> to.append("\"$value\"")
        // everything else
        else -> prettifyObject(value, to, indentationWidth, seen, depth + 1)
    }
}

private fun prettifyCollection(
    to: StringBuilder,
    value: Iterator<*>,
    indentationWidth: Int,
    seen: MutableSet<Any>,
    depth: Int
) = prettifyContainer(to, value.iterator(), depth, indentationWidth, enclosingCharacters = '[' to ']') { v ->
    prettifyValue(v, to, indentationWidth, seen, depth + 1)
}

private fun prettifyMap(
    to: StringBuilder,
    value: Map<*, *>,
    indentationWidth: Int,
    seen: MutableSet<Any>,
    depth: Int
) = prettifyContainer(to, value.iterator(), depth, indentationWidth, enclosingCharacters = '{' to '}') { (k, v) ->
    prettifyValue(k, to, indentationWidth, seen, depth + 1)
    to.append(" -> ")
    prettifyValue(v, to, indentationWidth, seen, depth + 1)
}

private fun <V> prettifyContainer(
    to: StringBuilder,
    iterator: Iterator<V>,
    depth: Int,
    indentationWidth: Int,
    enclosingCharacters: Pair<Char, Char>,
    itemRenderer: (V) -> Unit
) {
    // opening character
    to.append(enclosingCharacters.first)

    // add a line break after the opening character if there are values
    val hasValues = iterator.hasNext()
    if (hasValues) to.append(lineSeparator())

    // render each value
    iterator.forEach { v ->
        to.append(indent(depth + 1, indentationWidth))
        itemRenderer(v)
        to.append(",").append(lineSeparator())
    }

    // add the necessary indentation prior to the closing character
    if (hasValues) to.append(indent(depth, indentationWidth))

    // closing character
    to.append(enclosingCharacters.second)
}

private fun indent(depth: Int, width: Int) = " ".repeat(depth * width)

private fun Any.objectReference(): String = "@${toHexString(identityHashCode(this))}"
