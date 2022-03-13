@file:Suppress("ArrayInDataClass")

package pt.davidafsilva.jvm.kotlin

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.lang.System.identityHashCode
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.toKotlinDuration

class PrettifierTestSpec : DescribeSpec({

    describe("prettifying a null value") {
        it("should return the string \"null\"") {
            null.toPrettyString() shouldBe "null"
        }
    }

    describe("prettifying a 'primitive' type (Int)") {
        it("should return the value of the builtin toString()") {
            123.toPrettyString() shouldBe "123"
        }
    }

    describe("prettifying a non-data class") {
        class NonDataClass {
            override fun toString(): String = "some-string"
        }
        it("should return the value returned by the class's toString()") {
            NonDataClass().toPrettyString() shouldBe "some-string"
        }
    }

    context("prettifying a data class") {
        describe("with no nested data classes") {
            data class NoNesting(
                // "primitives"
                val char: Char = 'a',
                val str: String = "test",
                val bool: Boolean = true,
                val byte: Byte = 0x01,
                val short: Short = 2,
                val int: Int = 3,
                val double: Double = 4.1,
                val float: Float = 5.2f,
                // enum
                val enum: Enum<*> = LazyThreadSafetyMode.SYNCHRONIZED,
                // null
                val nullable: String? = null,
                // time
                val localDate: LocalDate = LocalDate.of(2022, Month.JANUARY, 1),
                val localTime: LocalTime = LocalTime.of(9, 0, 1),
                val localDateTime: LocalDateTime = LocalDateTime.of(localDate, localTime),
                val zonedDateTime: ZonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC+4")),
                val duration: Duration = Duration.ofSeconds(90),
                val ktDuration: kotlin.time.Duration = duration.toKotlinDuration(),
                // arrays
                val arr: Array<Any?> = arrayOf(localDate, duration),
                val charArray: CharArray = charArrayOf('a', 'b', 'c'),
                val byteArray: ByteArray = byteArrayOf(0x11, 0x22),
                val shortArray: ShortArray = shortArrayOf(1, 2),
                val intArray: IntArray = intArrayOf(3, 4),
                val longArray: LongArray = longArrayOf(5, 6),
                val doubleArray: DoubleArray = doubleArrayOf(6.6, 9.9),
                val floatArray: FloatArray = floatArrayOf(10.22f, 11.33f),
                // collections
                val list: List<Any?> = listOf("e1", null, "e2"),
                val set: Set<Any?> = setOf("e3", null, "e4"),
                // maps
                val map: Map<Any?, Any?> = mapOf(
                    1 to "value",
                    "key" to true,
                    null to null
                ),
            )

            val instance = NoNesting()
            it("should return the expected prettified string") {
                val expected = """
                    NoNesting@${instance.id()}(
                      char = 'a',
                      str = "test",
                      bool = true,
                      byte = 0x01,
                      short = 2,
                      int = 3,
                      double = 4.1,
                      float = 5.2,
                      enum = "SYNCHRONIZED",
                      nullable = null,
                      localDate = "2022-01-01",
                      localTime = "09:00:01",
                      localDateTime = "2022-01-01T09:00:01",
                      zonedDateTime = "2022-01-01T09:00:01+04:00[UTC+04:00]",
                      duration = "PT1M30S",
                      ktDuration = "1m 30s",
                      arr = [
                        "2022-01-01",
                        "PT1M30S",
                      ],
                      charArray = [
                        'a',
                        'b',
                        'c',
                      ],
                      byteArray = [
                        0x11,
                        0x22,
                      ],
                      shortArray = [
                        1,
                        2,
                      ],
                      intArray = [
                        3,
                        4,
                      ],
                      longArray = [
                        5,
                        6,
                      ],
                      doubleArray = [
                        6.6,
                        9.9,
                      ],
                      floatArray = [
                        10.22,
                        11.33,
                      ],
                      list = [
                        "e1",
                        null,
                        "e2",
                      ],
                      set = [
                        "e3",
                        null,
                        "e4",
                      ],
                      map = {
                        1 -> "value",
                        "key" -> true,
                        null -> null,
                      },
                    )
                    """.trimIndent()
                instance.toPrettyString() shouldBe expected
            }
        }

        describe("with nested data classes as members of the root data class") {
            data class Tree<V>(
                val value: V,
                val left: Tree<V?>? = null,
                val right: Tree<V?>? = null,
            )

            val instance = Tree(
                value = "ROOT",
                left = Tree(
                    value = "L", left = Tree(value = "LL"), right = Tree(value = "LR"),
                ),
                right = Tree(
                    value = "R",
                    left = Tree(value = "RL"),
                    right = Tree(value = "RR", left = Tree(value = "RRL"), right = Tree(value = "RRR")),
                ),
            )
            it("should return the expected prettified string") {
                val expected = """
                    Tree@${instance.id()}(
                      value = "ROOT",
                      left = Tree@${instance.left!!.id()}(
                        value = "L",
                        left = Tree@${instance.left.left!!.id()}(
                          value = "LL",
                          left = null,
                          right = null,
                        ),
                        right = Tree@${instance.left.right!!.id()}(
                          value = "LR",
                          left = null,
                          right = null,
                        ),
                      ),
                      right = Tree@${instance.right!!.id()}(
                        value = "R",
                        left = Tree@${instance.right.left!!.id()}(
                          value = "RL",
                          left = null,
                          right = null,
                        ),
                        right = Tree@${instance.right.right!!.id()}(
                          value = "RR",
                          left = Tree@${instance.right.right.left!!.id()}(
                            value = "RRL",
                            left = null,
                            right = null,
                          ),
                          right = Tree@${instance.right.right.right!!.id()}(
                            value = "RRR",
                            left = null,
                            right = null,
                          ),
                        ),
                      ),
                    )
                    """.trimIndent()
                instance.toPrettyString() shouldBe expected
            }
        }

        describe("with nested data classes within container objects (collections, arrays..)") {
            data class Date(
                val localDate: LocalDate = LocalDate.of(2022, Month.JANUARY, 1),
                val localTime: LocalTime = LocalTime.of(9, 0, 1),
            )

            val date1 = Date()
            val date2 = Date(date1.localDate.plusDays(1), date1.localTime.plusHours(1))

            data class DataClassCollections(
                val datesList: List<Date> = listOf(date1, date2),
                val datesSet: Set<Date> = setOf(date1.copy(), date2.copy()),
                val datesMapKey: Map<Date, Int> = mapOf(date1.copy() to 1, date2.copy() to 2),
                val datesMapValue: Map<Int, Date> = mapOf(1 to date1.copy(), 2 to date2.copy()),
                val datesMapKeyValue: Map<Date, Date> = mapOf(date1.copy() to date2.copy()),
            )

            val instance = DataClassCollections()
            it("should return the expected prettified string") {
                val expected = """
                    DataClassCollections@${instance.id()}(
                      datesList = [
                        Date@${instance.datesList.first().id()}(
                          localDate = "2022-01-01",
                          localTime = "09:00:01",
                        ),
                        Date@${instance.datesList.second().id()}(
                          localDate = "2022-01-02",
                          localTime = "10:00:01",
                        ),
                      ],
                      datesSet = [
                        Date@${instance.datesSet.first().id()}(
                          localDate = "2022-01-01",
                          localTime = "09:00:01",
                        ),
                        Date@${instance.datesSet.second().id()}(
                          localDate = "2022-01-02",
                          localTime = "10:00:01",
                        ),
                      ],
                      datesMapKey = {
                        Date@${instance.datesMapKey.entries.first().key.id()}(
                          localDate = "2022-01-01",
                          localTime = "09:00:01",
                        ) -> 1,
                        Date@${instance.datesMapKey.entries.second().key.id()}(
                          localDate = "2022-01-02",
                          localTime = "10:00:01",
                        ) -> 2,
                      },
                      datesMapValue = {
                        1 -> Date@${instance.datesMapValue.entries.first().value.id()}(
                          localDate = "2022-01-01",
                          localTime = "09:00:01",
                        ),
                        2 -> Date@${instance.datesMapValue.entries.second().value.id()}(
                          localDate = "2022-01-02",
                          localTime = "10:00:01",
                        ),
                      },
                      datesMapKeyValue = {
                        Date@${instance.datesMapKeyValue.entries.first().key.id()}(
                          localDate = "2022-01-01",
                          localTime = "09:00:01",
                        ) -> Date@${instance.datesMapKeyValue.entries.first().value.id()}(
                          localDate = "2022-01-02",
                          localTime = "10:00:01",
                        ),
                      },
                    )
                    """.trimIndent()
                instance.toPrettyString() shouldBe expected
            }
        }

        describe("with duplicated data classes instances (circular references)") {
            data class Node(val value: Int, val linksTo: List<Node> = emptyList())
            data class Graph(val nodes: List<Node>)

            val node1 = Node(1)
            val node2 = Node(2, linksTo = listOf(node1))
            val instance = Graph(listOf(node1, node2))

            it("should've detected the circular reference an correctly prettified its reference") {
                val expected = """
                    Graph@${instance.id()}(
                      nodes = [
                        Node@${node1.id()}(
                          value = 1,
                          linksTo = [],
                        ),
                        Node@${node2.id()}(
                          value = 2,
                          linksTo = [
                            <ref @${node1.id()}>,
                          ],
                        ),
                      ],
                    )
                    """.trimIndent()
                instance.toPrettyString() shouldBe expected
            }
        }
    }

    context("prettifying with a non-default indentation width") {
        data class Node(val next: Node? = null)

        val instance = Node(Node(Node(Node())))

        it("should return the expected prettified string respecting the defined indentation width") {
            val expected = """
                Node@${instance.id()}(
                        next = Node@${instance.next!!.id()}(
                                next = Node@${instance.next.next!!.id()}(
                                        next = Node@${instance.next.next.next!!.id()}(
                                                next = null,
                                        ),
                                ),
                        ),
                )
                """.trimIndent()
            instance.toPrettyString(indentationWidth = 8) shouldBe expected
        }
    }
})

private fun Any.id() = Integer.toHexString(identityHashCode(this))

private fun <T> Iterable<T>.second(): T {
    val iterator = iterator()
    if (!iterator.hasNext()) fail("Collection is empty")
    iterator.next()
    if (!iterator.hasNext()) fail("Collection has only a single element")
    return iterator.next()
}
