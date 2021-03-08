package yhsb.base.excel

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import yhsb.base.math.isValidInt
import java.io.ByteArrayInputStream
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

enum class Type {
    Xls, Xlsx, Auto
}

object Excel {
    fun load(fileName: String, type: Type = Type.Auto): Workbook {
        fun loadFile() = ByteArrayInputStream(
            Files.readAllBytes(Paths.get(fileName))
        )
        return when (type) {
            Type.Xls -> HSSFWorkbook(loadFile())
            Type.Xlsx -> XSSFWorkbook(loadFile())
            Type.Auto -> {
                val f = fileName.toLowerCase()
                when {
                    f.endsWith(".xls") -> HSSFWorkbook(loadFile())
                    f.endsWith(".xlsx") -> XSSFWorkbook(loadFile())
                    else -> throw Exception("unknown excel type: $f")
                }
            }
        }
    }

    fun load(path: Path) = load(path.toString(), Type.Auto)

    fun copyTo(source: Row, dest: Row, vararg fields: String) {
        for (field in fields) {
            dest.getOrCreateCell(field).setCellValue(
                source.getCell(field).getValue()
            )
        }
    }
}

fun Workbook.save(file: Path) {
    Files.newOutputStream(file).use { write(it) }
}

fun Workbook.save(fileName: String) = save(Paths.get(fileName))

fun Sheet.createRow(
    targetRowIndex: Int,
    sourceRowIndex: Int,
    clearValue: Boolean
): Row {
    if (targetRowIndex == sourceRowIndex) throw IllegalArgumentException(
        "sourceIndex and targetIndex cannot be same"
    )
    var newRow = getRow(targetRowIndex)
    val srcRow = getRow(sourceRowIndex)
    if (newRow == null) {
        //shiftRows(targetRowIndex, lastRowNum, 1, true, false)
        newRow = createRow(targetRowIndex)
    }

    newRow.height = srcRow.height
    for (idx in srcRow.firstCellNum until srcRow
        .lastCellNum) {
        val srcCell = srcRow.getCell(idx) ?: continue
        newRow.createCell(idx).run {
            cellStyle = srcCell.cellStyle
            cellComment = srcCell.cellComment
            hyperlink = srcCell.hyperlink
            if (clearValue) {
                setBlank()
            } else {
                when (srcCell.cellType) {
                    CellType.NUMERIC -> setCellValue(srcCell.numericCellValue)
                    CellType.STRING -> setCellValue(srcCell.stringCellValue)
                    CellType.FORMULA -> cellFormula = srcCell.cellFormula
                    CellType.BLANK -> setBlank()
                    CellType.BOOLEAN -> setCellValue(srcCell.booleanCellValue)
                    CellType.ERROR -> setCellErrorValue(srcCell.errorCellValue)
                    else -> {
                    }
                }
            }
        }
    }
    val merged = CellRangeAddressList()
    for (i in 0 until numMergedRegions) {
        val address = getMergedRegion(i)
        if (sourceRowIndex == address.firstRow
            && sourceRowIndex == address.lastRow
        ) {
            merged.addCellRangeAddress(
                targetRowIndex,
                address.firstColumn, targetRowIndex,
                address.lastColumn
            )
        }
    }
    for (region in merged.cellRangeAddresses) {
        addMergedRegion(region)
    }
    return newRow
}

/**
 * Get the row if [targetRowIndex] equals [sourceRowIndex] or
 * copy a row from the row of [sourceRowIndex].
 *
 * @param targetRowIndex the index of the row got or copied to, from 0.
 * @param sourceRowIndex the index of the row got or copied from, from 0.
 * @param clearValue if clear the values from the copied row.
 */
fun Sheet.getOrCopyRow(
    targetRowIndex: Int,
    sourceRowIndex: Int,
    clearValue: Boolean = true
): Row {
    return if (targetRowIndex == sourceRowIndex) {
        getRow(sourceRowIndex)
    } else {
        if (lastRowNum >= targetRowIndex)
            shiftRows(targetRowIndex, lastRowNum, 1, true, false)
        createRow(targetRowIndex, sourceRowIndex, clearValue)
    }
}

fun Sheet.copyRows(
    startRowIndex: Int,
    count: Int,
    sourceRowIndex: Int,
    clearValue: Boolean = false
) {
    shiftRows(startRowIndex, lastRowNum, count, true, false)
    for (i in 0 until count) {
        createRow(startRowIndex + i, sourceRowIndex, clearValue)
    }
}

fun Sheet.deleteRows(rowIndex: Int, count: Int) {
    val lastRowNum = lastRowNum
    if (rowIndex >= 0 && rowIndex <= lastRowNum && count > 0) {
        val endRowIndex = Math.min(rowIndex + count - 1, lastRowNum)
        (rowIndex..endRowIndex).forEach {
            val row = getRow(it)
            if (row != null) {
                removeRow(row)
            }
        }
        if (endRowIndex < lastRowNum) {
            shiftRows(endRowIndex + 1, lastRowNum, rowIndex - endRowIndex - 1)
        }
    }
}

fun Sheet.deleteRow(rowIndex: Int) = deleteRows(rowIndex, 1)

/**
 * get an iterator of rows
 * @param start the start row from 0 (included)
 * @param end the end row (included)
 */
fun Sheet.rowIterator(start: Int, end: Int = -1): Iterator<Row> {
    return object : Iterator<Row> {
        private var index = Math.max(0, start)
        private val last = if (end == -1) lastRowNum else Math.min(end, lastRowNum)

        override fun hasNext(): Boolean {
            return index <= last
        }

        override fun next(): Row {
            return getRow(index++)
        }
    }
}

/**
 * get the cell by address
 * @param address format "(\$?)([A-Z]+)(\$?)(\d+)"
 */
fun Sheet.getCell(address: String): Cell {
    val cell = CellRef.fromAddress(address)
    return getRow(cell.row - 1).getCell(cell.column - 1)
}

/**
 * get the cell by row and column
 * @param row from 0
 * @param column from 0
 */
fun Sheet.getCell(row: Int, column: Int): Cell = getRow(row).getCell(column)

fun Sheet.getCell(row: Int, columnName: String): Cell? =
    getRow(row).getCell(columnName)

/**
 * get cell from column name
 * @param column format "[A-Z]+"
 */
fun Row.getCell(column: String): Cell? = getCell(CellRef.columnNameToNumber(column) - 1)

fun Row.createCell(columnName: String): Cell = createCell(CellRef.columnNameToNumber(columnName) - 1)

fun Row.getOrCreateCell(column: Int): Cell {
    val cell = getCell(column)
    return cell ?: createCell(column)
}

fun Row.getOrCreateCell(columnName: String): Cell = getOrCreateCell(CellRef.columnNameToNumber(columnName) - 1)

fun Row.copyTo(dest: Row, vararg fields: String) {
    for (field in fields) {
        dest.getOrCreateCell(field).setValue(getCell(field).getValue())
    }
}

fun Cell?.getValue() = this?.getString(cellType) ?: ""

fun Cell.getString(type: CellType): String {
    return when (type) {
        CellType.STRING -> stringCellValue
        CellType.BOOLEAN -> booleanCellValue.toString()
        CellType.NUMERIC -> {
            val v = numericCellValue
            if (v.isValidInt()) {
                v.toInt().toString()
            } else {
                v.toString()
            }
        }
        CellType.FORMULA -> getString(cachedFormulaResultType)
        CellType.BLANK, CellType.ERROR -> ""
        else -> throw Exception("unsupported type: $type")
    }
}

fun Cell?.setValue(v: String?) = if (v != null) this?.setCellValue(v) else this?.setBlank()
fun Cell?.setValue(v: Double?) = if (v != null) this?.setCellValue(v) else this?.setBlank()
fun Cell?.setValue(v: BigDecimal?) = if (v != null) this?.setCellValue(v.toDouble()) else this?.setBlank()
fun Cell?.setValue(v: Int?) = if (v != null) this?.setCellValue(v.toDouble()) else this?.setBlank()

fun Cell?.setBlank() = this?.setBlank()

class CellRef(
    val row: Int, val column: Int,
    anchored: Boolean = false,
    rowAnchored: Boolean = false,
    columnAnchored: Boolean = false,
    columnName: String? = null
) {
    val columnName: String = columnName ?: columnNumberToName(column)
    val rowAnchored: Boolean = anchored || rowAnchored
    val columnAnchored: Boolean = anchored || columnAnchored

    companion object {
        val cellRegex = Regex("""(\$?)([A-Z]+)(\$?)(\d+)""")

        fun columnNumberToName(number: Int): String {
            var dividend = number
            var name = ""
            while (dividend > 0) {
                val modulo = (dividend - 1) % 26
                name = (65 + modulo).toChar().toString() + name
                dividend = (dividend - modulo) / 26
            }
            return name
        }

        fun columnNameToNumber(name: String): Int {
            var sum = 0
            for (c in name.toUpperCase()) {
                sum *= 26
                sum += c.toInt() - 64
            }
            return sum
        }

        fun fromAddress(address: String): CellRef {
            val match = cellRegex.find(address)
            if (match != null) {
                return CellRef(
                    columnAnchored = match.groupValues[1].isNotEmpty(),
                    columnName = match.groupValues[2],
                    column = columnNameToNumber(match.groupValues[2]),
                    rowAnchored = match.groupValues[3].isNotEmpty(),
                    row = match.groupValues[4].toInt()
                )
            }
            throw Exception("invalid cell address")
        }
    }

    fun toAddress(): String {
        var address = ""
        if (columnAnchored) address += "$"
        address += columnName
        if (rowAnchored) address += "$"
        address += row.toString()
        return address
    }
}
