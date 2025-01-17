@file:JvmName("FormattedCharSequenceTransformations")

package io.github.homchom.recode.ui.text

import net.minecraft.util.FormattedCharSequence

/**
 * @see CharSequence.subSequence
 */
fun FormattedCharSequence.subSequence(startIndex: Int, endIndex: Int) = FormattedCharSequence { sink ->
    var index = 0
    var adjustedIndex = 0
    accept { _, style, codePoint ->
        if (index++ in startIndex..<endIndex) {
            sink.accept(adjustedIndex++, style, codePoint)
        } else true
    }
}

/**
 * @see CharSequence.replaceRange
 */
fun FormattedCharSequence.replaceRange(
    range: IntRange,
    replacement: FormattedCharSequence
): FormattedCharSequence {
    return FormattedCharSequence { sink ->
        var index = 0
        var adjustedIndex = 0
        accept { _, style, codePoint ->
            when (index++) {
                range.first -> replacement.accept { _, style2, codePoint2 ->
                    sink.accept(adjustedIndex++, style2, codePoint2)
                }
                in range -> true
                else -> sink.accept(adjustedIndex++, style, codePoint)
            }
        }
    }
}