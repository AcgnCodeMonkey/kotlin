/*
 * Copyright 2010-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlin

import java.util.Comparator
import kotlin.platform.platformName

/**
 * Compares two values using the specified sequence of functions to calculate the result of the comparison.
 * The functions are called sequentially, receive the given values [a] and [b] and return [Comparable]
 * objects. As soon as the [Comparable] instances returned by a function for [a] and [b] values do not
 * compare as equal, the result of that comparison is returned.
 */
deprecated("Use selector functions accepting nullable T as a receiver.")
public fun <T : Any> compareValuesBy(a: T?, b: T?, vararg functions: (T) -> Comparable<*>?): Int {
    require(functions.size() > 0)
    if (a === b) return 0
    if (a == null) return -1
    if (b == null) return 1
    for (fn in functions) {
        val v1 = fn(a)
        val v2 = fn(b)
        val diff = compareValues(v1, v2)
        if (diff != 0) return diff
    }
    return 0
}

/**
 * Compares two values using the specified functions [selectors] to calculate the result of the comparison.
 * The functions are called sequentially, receive the given values [a] and [b] and return [Comparable]
 * objects. As soon as the [Comparable] instances returned by a function for [a] and [b] values do not
 * compare as equal, the result of that comparison is returned.
 */
// TODO: Remove platformName after M13
platformName("compareValuesByNullable")
public fun <T> compareValuesBy(a: T, b: T, vararg selectors: (T) -> Comparable<*>?): Int {
    require(selectors.size() > 0)
    for (fn in selectors) {
        val v1 = fn(a)
        val v2 = fn(b)
        val diff = compareValues(v1, v2)
        if (diff != 0) return diff
    }
    return 0
}

/**
 * Compares two values using the specified [selector] function to calculate the result of the comparison.
 * The function is applied to the given values [a] and [b] and return [Comparable] objects.
 * The result of comparison of these [Comparable] instances is returned.
 */
public inline fun <T> compareValuesBy(a: T, b: T, selector: (T) -> Comparable<*>?): Int {
    return compareValues(selector(a), selector(b))
}

/**
 * Compares two values using the specified [selector] function to calculate the result of the comparison.
 * The function is applied to the given values [a] and [b] and return objects of type K which are then being
 * compared with the given [comparator].
 */
public inline fun <T, K> compareValuesBy(a: T, b: T, comparator: Comparator<in K>, selector: (T) -> K): Int {
    return comparator.compare(selector(a), selector(b))
}

//// Not so useful without type inference for receiver of expression
//// compareValuesWith(v1, v2, compareBy { it.prop1 } thenByDescending { it.prop2 })
///**
// * Compares two values using the specified [comparator].
// */
//@suppress("NOTHING_TO_INLINE")
//public inline fun <T> compareValuesWith(a: T, b: T, comparator: Comparator<T>): Int = comparator.compare(a, b)
//


/**
 * Compares two nullable [Comparable] values. Null is considered less than any value.
 */
public fun <T : Comparable<*>> compareValues(a: T?, b: T?): Int {
    if (a === b) return 0
    if (a == null) return -1
    if (b == null) return 1

    return (a as Comparable<Any?>).compareTo(b)
}

/**
 * Creates a comparator using the sequence of functions to calculate a result of comparison.
 * The functions are called sequentially, receive the given values `a` and `b` and return [Comparable]
 * objects. As soon as the [Comparable] instances returned by a function for `a` and `b` values do not
 * compare as equal, the result of that comparison is returned from the [Comparator].
 */
public fun <T> compareBy(vararg selectors: (T) -> Comparable<*>?): Comparator<T> {
    return object : Comparator<T> {
        public override fun compare(a: T, b: T): Int = compareValuesBy(a, b, *selectors)
    }
}

/**
 * Creates a comparator using the sequence of functions to calculate a result of comparison.
 */
deprecated("Use compareBy() instead", ReplaceWith("compareBy(*functions)"))
public fun <T> comparator(vararg functions: (T) -> Comparable<*>?): Comparator<T> = compareBy(*functions)

/**
 * Creates a comparator using the function to transform value to a [Comparable] instance for comparison.
 */
public fun <T> compareBy(inlineOptions(InlineOption.ONLY_LOCAL_RETURN) selector: (T) -> Comparable<*>?): SelectorComparator<T, Comparable<*>?> {
    return SelectorComparator(
            selector,
            nullsFirst(naturalOrder<Comparable<Any?>>()) as Comparator<Comparable<*>?>)
}

/**
 * Creates a comparator using the [selector] function to transform values being compared and then applying
 * the specified [comparator] to compare transformed values.
 */
public fun <T, K> compareBy(comparator: Comparator<in K>, inlineOptions(InlineOption.ONLY_LOCAL_RETURN) selector: (T) -> K): SelectorComparator<T, K> {
    return SelectorComparator(selector, comparator)
}

/**
 * Creates a descending comparator using the function to transform value to a [Comparable] instance for comparison.
 */
public fun <T> compareByDescending(inlineOptions(InlineOption.ONLY_LOCAL_RETURN) selector: (T) -> Comparable<*>?): Comparator<T> {
    return SelectorComparator(
            selector,
            nullsLast(reverseOrder<Comparable<Any?>>()) as Comparator<Comparable<*>?>)
}

/**
 * Creates a descending comparator using the [selector] function to transform values being compared and then applying
 * the specified [comparator] to compare transformed values.
 *
 * Note that an order of [comparator] is reversed by this wrapper.
 */
public fun <T, K> compareByDescending(comparator: Comparator<in K>, inlineOptions(InlineOption.ONLY_LOCAL_RETURN) selector: (T) -> K): SelectorComparator<T, K> {
    return SelectorComparator(selector, comparator.reversed())
}

/**
 * Creates a comparator comparing values after the primary comparator defined them equal. It uses
 * the function to transform value to a [Comparable] instance for comparison.
 */
inline public fun <T> Comparator<T>.thenBy(inlineOptions(InlineOption.ONLY_LOCAL_RETURN) selector: (T) -> Comparable<*>?): Comparator<T> {
    return object : Comparator<T> {
        public override fun compare(a: T, b: T): Int {
            val previousCompare = this@thenBy.compare(a, b)
            return if (previousCompare != 0) previousCompare else compareValuesBy(a, b, selector)
        }
    }
}

/**
 * Creates a comparator comparing values after the primary comparator defined them equal. It uses
 * the [selector] function to transform values and then compares them with the given [comparator].
 */
inline public fun <T, K> Comparator<T>.thenBy(comparator: Comparator<in K>, inlineOptions(InlineOption.ONLY_LOCAL_RETURN) selector: (T) -> K): Comparator<T> {
    return object : Comparator<T> {
        public override fun compare(a: T, b: T): Int {
            val previousCompare = this@thenBy.compare(a, b)
            return if (previousCompare != 0) previousCompare else compareValuesBy(a, b, comparator, selector)
        }
    }
}

/**
 * Creates a descending comparator using the primary comparator and
 * the function to transform value to a [Comparable] instance for comparison.
 */
inline public fun <T> Comparator<T>.thenByDescending(inlineOptions(InlineOption.ONLY_LOCAL_RETURN) selector: (T) -> Comparable<*>?): Comparator<T> {
    return object : Comparator<T> {
        public override fun compare(a: T, b: T): Int {
            val previousCompare = this@thenByDescending.compare(a, b)
            return if (previousCompare != 0) previousCompare else compareValuesBy(b, a, selector)
        }
    }
}

/**
 * Creates a descending comparator comparing values after the primary comparator defined them equal. It uses
 * the [selector] function to transform values and then compares them with the given [comparator].
 */
inline public fun <T, K> Comparator<T>.thenByDescending(comparator: Comparator<in K>, inlineOptions(InlineOption.ONLY_LOCAL_RETURN) selector: (T) -> K): Comparator<T> {
    return object : Comparator<T> {
        public override fun compare(a: T, b: T): Int {
            val previousCompare = this@thenByDescending.compare(a, b)
            return if (previousCompare != 0) previousCompare else compareValuesBy(b, a, comparator, selector)
        }
    }
}


/**
 * Creates a comparator using the function to calculate a result of comparison.
 */
inline public fun <T> comparator(inlineOptions(InlineOption.ONLY_LOCAL_RETURN) comparison: (T, T) -> Int): Comparator<T> {
    return object : Comparator<T> {
        public override fun compare(a: T, b: T): Int = comparison(a, b)
    }
}

/**
 * Creates a comparator using the primary comparator and function to calculate a result of comparison.
 */
inline public fun <T> Comparator<T>.thenComparator(inlineOptions(InlineOption.ONLY_LOCAL_RETURN) comparison: (T, T) -> Int): Comparator<T> {
    return object : Comparator<T> {
        public override fun compare(a: T, b: T): Int {
            val previousCompare = this@thenComparator.compare(a, b)
            return if (previousCompare != 0) previousCompare else comparison(a, b)
        }
    }
}

/**
 * Combines this comparator and the given [comparator] such that the latter is applied only
 * when the former considered values equal.
 */
public fun <T> Comparator<T>.then(comparator: Comparator<in T>): Comparator<T> {
    return object : Comparator<T> {
        public override fun compare(a: T, b: T): Int {
            val previousCompare = this@then.compare(a, b)
            return if (previousCompare != 0) previousCompare else comparator.compare(a, b)
        }
    }
}

/**
 * Combines this comparator and the given [comparator] such that the latter is applied only
 * when the former considered values equal.
 */
public fun <T> Comparator<T>.thenDescending(comparator: Comparator<in T>): Comparator<T> {
    return object : Comparator<T> {
        public override fun compare(a: T, b: T): Int {
            val previousCompare = this@thenDescending.compare(a, b)
            return if (previousCompare != 0) previousCompare else comparator.compare(b, a)
        }
    }
}

// Not so useful without type inference for receiver of expression
/**
 * Extends the given [comparator] of non-nullable values to a comparator of nullable values
 * considering `null` value less than any other value.
 */
public fun <T: Any> nullsFirst(comparator: Comparator<in T>): Comparator<T?> = when (comparator) {
    is NullsComparator<*> -> NullsComparator((comparator as NullsComparator<in T>).comparator, nullIsGreater = false)
    else -> NullsComparator(comparator, nullIsGreater = false)
}

/**
 * Provides a comparator of nullable [Comparable] values
 * considering `null` value less than any other value.
 */
public fun <T: Comparable<T>> nullsFirst(): Comparator<T?> {
    return nullsFirst(naturalOrder())
}

/**
 * Extends the given [comparator] of non-nullable values to a comparator of nullable values
 * considering `null` value greater than any other value.
 */
public fun <T: Any> nullsLast(comparator: Comparator<in T>): Comparator<T?> = when (comparator) {
    is NullsComparator<*> -> NullsComparator((comparator as NullsComparator<in T>).comparator, nullIsGreater = true)
    else -> NullsComparator(comparator, nullIsGreater = true)
}

/**
 * Provides a comparator of nullable [Comparable] values
 * considering `null` value greater than any other value.
 */
public fun <T: Comparable<T>> nullsLast(): Comparator<T?> {
    return nullsLast(naturalOrder())
}

/** Returns a comparator that imposes the reverse ordering of this comparator. */
public fun <T> Comparator<T>.reversed(): Comparator<T> = when (this) {
    is ReversedComparator -> this.comparator
    is NullsComparator<*> -> NullsComparator(((this as NullsComparator<T>).comparator).reversed(), nullIsGreater = !nullIsGreater) as Comparator<T>
    else -> ReversedComparator(this)
}

private class ReversedComparator<T>(public val comparator: Comparator<T>): Comparator<T> {
    override fun compare(a: T, b: T): Int = comparator.compare(b, a)
}


private fun <T: Comparable<T>> naturalOrder(): Comparator<T> = object : Comparator<T> {
    override fun compare(a: T, b: T): Int = a.compareTo(b)
}

private fun <T: Comparable<T>> reverseOrder(): Comparator<T> = object : Comparator<T> {
    override fun compare(a: T, b: T): Int = b.compareTo(a)
}



private class NullsComparator<T>(public val comparator: Comparator<in T>, public val nullIsGreater: Boolean = false) : Comparator<T?> {
    override fun compare(a: T?, b: T?): Int {
        if (a === b) return 0
        if (a == null) return if (nullIsGreater) 1 else -1
        if (b == null) return if (nullIsGreater) -1 else 1
        return comparator.compare(a, b)
    }
}


public class SelectorComparator<T, K>(public val selector: (T) -> K, public val comparator: Comparator<in K>): Comparator<T> {
    override fun compare(a: T, b: T): Int = comparator.compare(selector(a), selector(b))
}

public fun <T, K> SelectorComparator<T, K>.nullsFirst(): SelectorComparator<T, K>
        = SelectorComparator<T, K>(selector, nullsFirst(comparator))

public fun <T, K> SelectorComparator<T, K>.nullsLast(): SelectorComparator<T, K>
        = SelectorComparator<T, K>(selector, nullsLast(comparator))
