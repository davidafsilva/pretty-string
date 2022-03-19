package pt.davidafsilva.jvm.kotlin

import kotlin.reflect.KClass

interface PrettifyContext {
    fun depth(): Int
    fun indentationWidth(): Int
    fun write(value: String)
}

fun interface Prettifier<in V> {
    fun prettify(value: V, ctx: PrettifyContext)
}

abstract class ClassPrettifier<C : Any>(
    internal val clazz: KClass<C>
) : Prettifier<C> {
    internal fun canPrettify(value: Any?) = clazz.isInstance(value)
}

abstract class IndexableClassPrettifier<C : Any>(
    clazz: KClass<C>
) : ClassPrettifier<C>(clazz) {
    init {
        require(!clazz.isAbstract && !clazz.isSealed) {
            "${clazz.simpleName} is abstract: only instantiable / final classes should be used"
        }
    }
}

//----------------------
// built-in prettifiers
//----------------------

internal val NullPrettifier = Prettifier<Any?> { _, ctx -> ctx.write("null") }

internal object StringPrettifier : IndexableClassPrettifier<String>(String::class) {
    override fun prettify(value: String, ctx: PrettifyContext) {
        ctx.write("\"$value\"")
    }
}

internal object EnumPrettifier : ClassPrettifier<Enum<*>>(Enum::class) {
    override fun prettify(value: Enum<*>, ctx: PrettifyContext) {
        ctx.write("\"${value.name}\"")
    }
}

internal object CharPrettifier : IndexableClassPrettifier<Char>(Char::class) {
    override fun prettify(value: Char, ctx: PrettifyContext) {
        ctx.write("'$value'")
    }
}

internal object CollectionPrettifier : ClassPrettifier<>
