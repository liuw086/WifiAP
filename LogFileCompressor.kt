package github.leavesczy.wifip2p.utils

import android.annotation.SuppressLint
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.nio.file.Files
import java.util.zip.CRC32
import java.util.zip.ZipFile

class LogFileCompressor {


    private val BUFFER_SIZE = 128 * 1024 // 64KB 缓冲区

    @SuppressLint("NewApi")
    fun turboPack(sourceDir: File, zipFile: File) {
        val buffer = ByteArray(BUFFER_SIZE)
        val crc = CRC32()

        BufferedOutputStream(FileOutputStream(zipFile), BUFFER_SIZE).use { bos ->
            ZipOutputStream(bos).apply {
                setMethod(ZipOutputStream.STORED) // 必须使用 STORED 模式
//                setLevel(Deflater.BEST_SPEED)
            }.use { zos ->
                Files.walk(sourceDir.toPath()).parallel().forEach { path ->
                    val file = path.toFile()
                    if (!file.isDirectory) {
                        val relativePath = sourceDir.toPath().relativize(path).toString()
                            .replace(File.separatorChar, '/')

                        // 预计算元数据
                        FileInputStream(file).use { fis ->
                            crc.reset()
                            var bytesRead: Int
                            while (fis.read(buffer).also { bytesRead = it } != -1) {
                                crc.update(buffer, 0, bytesRead)
                            }
                        }

                        // 创建条目并设置关键参数
                        val entry = ZipEntry(relativePath).apply {
                            size = file.length()
                            compressedSize = file.length()
                            method = ZipEntry.STORED
                            this.crc = crc.value
                        }

                        zos.putNextEntry(entry)
                        FileInputStream(file).buffered(BUFFER_SIZE).use {
                            it.copyTo(zos)
                        }
                        zos.closeEntry()
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    fun turboUnpack(zipFile: File, destDir: File) {
        val buffer = ByteArray(64 * 1024) // 64KB 缓冲区

        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val targetFile = File(destDir, entry.name)

                if (entry.isDirectory) {
                    Files.createDirectories(targetFile.toPath())
                } else {
                    Files.createDirectories(targetFile.parentFile.toPath())
                    zip.getInputStream(entry).use { input ->
                        BufferedInputStream(input).use { bis ->
                            FileOutputStream(targetFile).use { fos ->
                                var bytesRead: Int
                                while (bis.read(buffer).also { bytesRead = it } != -1) {
                                    fos.write(buffer, 0, bytesRead)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val BufferExtraCount = 100
    }
}
