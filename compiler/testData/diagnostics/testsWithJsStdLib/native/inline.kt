// !DIAGNOSTICS: -NOTHING_TO_INLINE

<!INLINE_EXTERNAL_DECLARATION!>inline external fun foo(): Unit<!>

inline external val bar: Int
    <!INLINE_EXTERNAL_DECLARATION!>get()<!> = noImpl

external val baz: Int
    <!INLINE_EXTERNAL_DECLARATION!>inline get()<!> = noImpl

external class A {
    <!INLINE_EXTERNAL_DECLARATION!>inline fun foo(): Unit<!>

    inline val bar: Int
        <!INLINE_EXTERNAL_DECLARATION!>get()<!> = noImpl

    val baz: Int
        <!INLINE_EXTERNAL_DECLARATION!>inline get()<!> = noImpl
}