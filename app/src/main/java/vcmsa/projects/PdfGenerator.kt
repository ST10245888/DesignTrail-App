package vcmsa.projects.fkj_consultants.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import vcmsa.projects.fkj_consultants.models.Quotation
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateQuotationPdf(context: Context, quotation: Quotation): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Simple text example (replace with full formatting)
        canvas.drawText("Quotation for: ${quotation.companyName}", 50f, 50f, android.graphics.Paint())

        pdfDocument.finishPage(page)

        val file = File(context.filesDir, "quotation_${quotation.id}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        return file
    }
}
