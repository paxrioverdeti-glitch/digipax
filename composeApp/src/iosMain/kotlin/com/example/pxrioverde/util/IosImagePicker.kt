package com.example.pxrioverde.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleActionSheet
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.math.max
import kotlin.math.min

/**
 * Camera e galeria do iPhone, usando o seletor nativo da Apple (UIImagePickerController).
 * A foto volta ja reduzida e em JPEG, pronta para enviar.
 *
 * Permissoes: o Info.plist ja declara NSCameraUsageDescription. A galeria pelo seletor
 * nativo nao precisa de permissao (o iOS mostra so as fotos que a pessoa escolher).
 */
object IosImagePicker {

    enum class Source { CAMERA, GALLERY }

    // O iOS guarda o "delegate" de forma fraca: sem esta referencia o Kotlin poderia
    // liberar o objeto antes da pessoa escolher a foto, e o resultado nunca chegaria.
    private var activeDelegate: PickerDelegate? = null

    fun isCameraAvailable(): Boolean =
        UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)

    /**
     * Abre a camera ou a galeria. [onResult] recebe os bytes JPEG, ou null se a pessoa
     * cancelar ou der erro. Se a camera nao existir (ex.: simulador), abre a galeria.
     */
    fun present(
        source: Source,
        maxSide: Int = 1024,
        quality: Double = 0.75,
        allowGalleryFallback: Boolean = true,
        onResult: (ByteArray?) -> Unit
    ) {
        val top = topViewController() ?: run { onResult(null); return }
        if (source == Source.CAMERA && !isCameraAvailable() && !allowGalleryFallback) {
            IosAlert.show("Câmera indisponível", "Este aparelho não tem câmera disponível para tirar a foto.")
            onResult(null)
            return
        }
        val useCamera = source == Source.CAMERA && isCameraAvailable()

        val picker = UIImagePickerController()
        picker.sourceType = if (useCamera) {
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        } else {
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        }
        picker.allowsEditing = false

        val delegate = PickerDelegate { image ->
            activeDelegate = null
            onResult(image?.toJpegBytes(maxSide, quality))
        }
        activeDelegate = delegate
        picker.delegate = delegate
        top.presentViewController(picker, animated = true, completion = null)
    }

    /**
     * Menu nativo do iPhone (action sheet) com "Tirar foto" e "Escolher da galeria".
     * Se a camera nao existir, o menu mostra so a galeria.
     */
    fun presentChooser(
        title: String? = null,
        maxSide: Int = 1024,
        quality: Double = 0.75,
        onResult: (ByteArray?) -> Unit
    ) {
        val top = topViewController() ?: run { onResult(null); return }
        val sheet = UIAlertController.alertControllerWithTitle(
            title = title,
            message = null,
            preferredStyle = UIAlertControllerStyleActionSheet
        )
        if (isCameraAvailable()) {
            sheet.addAction(UIAlertAction.actionWithTitle("Tirar foto", UIAlertActionStyleDefault) { _ ->
                present(Source.CAMERA, maxSide, quality, onResult = onResult)
            })
        }
        sheet.addAction(UIAlertAction.actionWithTitle("Escolher da galeria", UIAlertActionStyleDefault) { _ ->
            present(Source.GALLERY, maxSide, quality, onResult = onResult)
        })
        sheet.addAction(UIAlertAction.actionWithTitle("Cancelar", UIAlertActionStyleCancel) { _ ->
            onResult(null)
        })
        top.presentViewController(sheet, animated = true, completion = null)
    }

    private fun topViewController(): UIViewController? = IosAlert.topViewController()
}

/** Aviso nativo do iPhone (equivalente ao Toast do Android), com um botão "OK". */
object IosAlert {
    fun show(title: String, message: String) {
        val top = topViewController() ?: return
        val alert = UIAlertController.alertControllerWithTitle(
            title = title,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert
        )
        alert.addAction(UIAlertAction.actionWithTitle("OK", UIAlertActionStyleDefault, null))
        top.presentViewController(alert, animated = true, completion = null)
    }

    fun topViewController(): UIViewController? {
        var vc = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (vc?.presentedViewController != null) {
            vc = vc.presentedViewController
        }
        return vc
    }
}

private class PickerDelegate(
    private val onDone: (UIImage?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        picker.dismissViewControllerAnimated(true) { onDone(image) }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) { onDone(null) }
    }
}

/**
 * Reduz a imagem para no maximo [maxSide] pixels no maior lado e gera JPEG.
 * Redesenhar a imagem tambem "endireita" a foto da camera (orientacao do sensor).
 */
@OptIn(ExperimentalForeignApi::class)
internal fun UIImage.toJpegBytes(maxSide: Int, quality: Double): ByteArray? {
    val (width, height) = size.useContents { width to height }
    if (width <= 0.0 || height <= 0.0) return null
    val scale = min(1.0, maxSide.toDouble() / max(width, height))
    val targetW = width * scale
    val targetH = height * scale

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetW, targetH), false, 1.0)
    drawInRect(CGRectMake(0.0, 0.0, targetW, targetH))
    val resized = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    val data = UIImageJPEGRepresentation(resized ?: this, quality) ?: return null
    return data.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val result = ByteArray(size)
    if (size > 0) {
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
    return result
}
