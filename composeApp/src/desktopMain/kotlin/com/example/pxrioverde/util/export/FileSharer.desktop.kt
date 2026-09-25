package com.example.pxrioverde.util.export

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class FileSharer {
    actual fun shareCsv(content: String, fileName: String) {
        val chooser = JFileChooser().apply {
            dialogTitle = "Salvar CSV"
            fileFilter = FileNameExtensionFilter("Arquivos CSV", "csv")
            selectedFile = File("$fileName.csv")
        }
        
        val userSelection = chooser.showSaveDialog(null)
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            val fileToSave = chooser.selectedFile
            fileToSave.writeText(content)
        }
    }
}
