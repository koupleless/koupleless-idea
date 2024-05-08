package com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil.spyBundle
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/6 15:35
 */
class FileParseUtilTest {
    @Test
    fun testParsePackageName(){
        // 验证 null
        assertNull(FileParseUtil.parsePackageName(null))

        // 正常：java 文件路径
        val path1 = StrUtil.join(FileUtil.FILE_SEPARATOR,"Users","mockDir","appName","app","bootstrap","src","main","java","com","mock","module","MockA.java")
        assertEquals("com.mock.module",FileParseUtil.parsePackageName(path1))

        // 异常情况1：包路径
        val path2 = StrUtil.join(FileUtil.FILE_SEPARATOR,"Users","mockDir","appName","app","bootstrap","src","main")
        assertNull(FileParseUtil.parsePackageName(path2))

        // 异常情况2: 无packageName 的 java 文件
        val path3 = StrUtil.join(FileUtil.FILE_SEPARATOR,"Users","mockDir","appName","app","bootstrap","src","MockA.java")
        assertNull(FileParseUtil.parsePackageName(path3))
    }

    @Test
    fun testParseDefaultPackageName(){
        val bundle = spyBundle("mockPath","mockBundle","com.mock.module")
        val parsedPackageName = FileParseUtil.parseDefaultPackageName(bundle)
        assertEquals("com.mock.module",parsedPackageName)
    }

    @Test
    fun testParsePackageRoot(){
        val bundle = spyBundle("mockPath","mockBundle","com.mock.module")
        val parsePackageRoot = FileParseUtil.parsePackageRoot(bundle)
        val path = StrUtil.join(FileUtil.FILE_SEPARATOR,"mockPath","mockBundle","src","main","java","com","mock","module")
        assertEquals(path,parsePackageRoot.path)
    }

    @Test
    fun testParseResourceRoot(){
        val parseResourceRoot = FileParseUtil.parseResourceRoot("mockBundle")
        val path = StrUtil.join(FileUtil.FILE_SEPARATOR,"mockBundle","src","main","resources")
        assertEquals(path,parseResourceRoot.path)
    }

    @Test
    fun testParseJavaRoot(){
        val parseJavaRoot = FileParseUtil.parseJavaRoot("mockBundle")
        val path = StrUtil.join(FileUtil.FILE_SEPARATOR,"mockBundle","src","main","java")
        assertEquals(path,parseJavaRoot.path)
    }

    @Test
    fun testParseMainRoot(){
        val parseMainRoot = FileParseUtil.parseMainRoot("mockBundle")
        val path = StrUtil.join(FileUtil.FILE_SEPARATOR,"mockBundle","src","main")
        assertEquals(path,parseMainRoot.path)
    }

    @Test
    fun testParsePomByBundle(){
        val parsePomOfBundle = FileParseUtil.parsePomByBundle("mockBundle")
        val path = StrUtil.join(FileUtil.FILE_SEPARATOR,"mockBundle","pom.xml")
        assertEquals(path,parsePomOfBundle.path)
    }

    @Test
    fun testParsePomByFile(){
        val path = StrUtil.join(FileUtil.FILE_SEPARATOR,"mockBundle","src","main","java","com","mock","module","MockClass.java")
        val parsedPomPath = FileParseUtil.parsePomByFile(path)
        assertEquals(StrUtil.join(FileUtil.FILE_SEPARATOR,"mockBundle","pom.xml"),parsedPomPath)
    }

    @Test
    fun testParseBundlePath(){
        // java 文件
        val javaPath = StrUtil.join(FileUtil.FILE_SEPARATOR,"mockBundle","src","main","java","com","mock","module","MockClass.java")
        val parsedBundlePath = FileParseUtil.parseBundlePath(javaPath)
        assertEquals("mockBundle",parsedBundlePath)

        // pom 文件
        val pomPath = StrUtil.join(FileUtil.FILE_SEPARATOR,"mockBundle","pom.xml")
        val parsedBundlePath2 = FileParseUtil.parseBundlePath(pomPath)
        assertEquals("mockBundle",parsedBundlePath2)

        // resources 文件
        val resourcesPath = StrUtil.join(FileUtil.FILE_SEPARATOR,"mockBundle","src","main","resources","mock.properties")
        val parsedBundlePath3 = FileParseUtil.parseBundlePath(resourcesPath)
        assertEquals("mockBundle",parsedBundlePath3)

        // TODO 异常情况：不属于任何bundle下
    }

    @Test
    fun testIsBundle(){
        // 不是 bundle
        assertFalse(FileParseUtil.isBundle("mockPath"))

        // 是 bundle
        mockkObject(FileParseUtil){
            every { FileParseUtil.parsePomByBundle(any()) } returns MockKUtil.spyFile("mockPath"+FileUtil.FILE_SEPARATOR+"pom.xml")
            assertTrue(FileParseUtil.isBundle("mockPath"))
        }
    }

    @Test
    fun testParseRelativePath(){
        val res = FileParseUtil.parseRelativePath("mockPath",StrUtil.join(FileUtil.FILE_SEPARATOR,"mockPath","mockBundle","pom.xml"))
        assertEquals("mockBundle"+FileUtil.FILE_SEPARATOR+"pom.xml",res)
    }

    @Test
    fun testListDirectory(){
        val dirA = MockKUtil.spyDir("mockPath", listOf("dirA","dirB"))
        assertEquals(1,FileParseUtil.listDirectory(dirA).size)
    }

    @Test
    fun testListValidFiles(){
        val mockDir = MockKUtil.spyDir("mockDir", arrayOf(MockKUtil.spyFile("Mock.java"),
            MockKUtil.spyFile(".properties"),
            MockKUtil.spyFile("abc.iml")))

        val res = FileParseUtil.listValidFiles(mockDir)
        assertEquals(1,res.size)
    }

    @Test
    fun testIsResourceDir(){
        val notResourceDir = MockKUtil.spyDir("mockDir")
        assertFalse(FileParseUtil.isResourceDir(notResourceDir))

        val file = MockKUtil.spyFile("mockFile")
        assertFalse(FileParseUtil.isResourceDir(file))

        val resourceDir = MockKUtil.spyDir(StrUtil.join(FileUtil.FILE_SEPARATOR,"src","main","resources","mockDir"))
        assertTrue(FileParseUtil.isResourceDir(resourceDir))

        val resourceFile = MockKUtil.spyFile(StrUtil.join(FileUtil.FILE_SEPARATOR,"src","main","resources","mockDir","mockFile.properties"))
        assertFalse(FileParseUtil.isResourceDir(resourceFile))
    }

    @Test
    fun testIsResourceFile(){
        val notResourceDir = MockKUtil.spyDir("mockDir")
        assertFalse(FileParseUtil.isResourceFile(notResourceDir))

        val file = MockKUtil.spyFile("mockFile")
        assertFalse(FileParseUtil.isResourceFile(file))

        val resourceDir = MockKUtil.spyDir(StrUtil.join(FileUtil.FILE_SEPARATOR,"src","main","resources","mockDir"))
        assertFalse(FileParseUtil.isResourceFile(resourceDir))

        val resourceFile = MockKUtil.spyFile(StrUtil.join(FileUtil.FILE_SEPARATOR,"src","main","resources","mockDir","mockFile.properties"))
        assertTrue(FileParseUtil.isResourceFile(resourceFile))
    }


    @Test
    fun testFolderContainsFile(){
        val folder = MockKUtil.spyDir(StrUtil.join(FileUtil.FILE_SEPARATOR,"mockDir","mockBundle"))
        assertTrue(FileParseUtil.folderContainsFile(folder,folder))

        val fileInFolder = MockKUtil.spyFile(StrUtil.join(FileUtil.FILE_SEPARATOR,"mockDir","mockBundle","pom.xml"))
        assertTrue(FileParseUtil.folderContainsFile(folder,fileInFolder))

        val fileNotInFolder = MockKUtil.spyFile(StrUtil.join(FileUtil.FILE_SEPARATOR,"mockDir","pom.xml"))
        assertFalse(FileParseUtil.folderContainsFile(folder,fileNotInFolder))
    }

    @Test
    fun testFileInFolder(){
        assertTrue(FileParseUtil.fileInFolder("mockDir"+FileUtil.FILE_SEPARATOR+"mockFile","mockDir"))

        assertFalse(FileParseUtil.fileInFolder("mockDir"+FileUtil.FILE_SEPARATOR+"mockFile","mockDir"+FileUtil.FILE_SEPARATOR+"mockFile"))

        assertFalse(FileParseUtil.fileInFolder("mockDir"+FileUtil.FILE_SEPARATOR+"mockFile","mockDir1"))

        assertFalse(FileParseUtil.fileInFolder("mockDir"+FileUtil.FILE_SEPARATOR+"mockFile","mockDir"+FileUtil.FILE_SEPARATOR+"mockFile1"))
    }

    @Test
    fun testIsNormalFolder(){
        assertFalse(FileParseUtil.isNormalFolder(MockKUtil.spyFile("mockFile")))

        mockkObject(FileParseUtil){
            every { FileParseUtil.isBundle(any()) } returns true
            assertFalse(FileParseUtil.isNormalFolder(MockKUtil.spyDir("mockDir")))
        }

        mockkObject(FileParseUtil){
            every { FileParseUtil.isBundle(any()) } returns false
            every { FileParseUtil.isResourceDir(any()) } returns true
            assertFalse(FileParseUtil.isNormalFolder(MockKUtil.spyDir("mockDir")))
        }

        mockkObject(FileParseUtil){
            every { FileParseUtil.isBundle(any()) } returns false
            every { FileParseUtil.isResourceDir(any()) } returns false
            every { FileParseUtil.isPackage(any()) } returns true
            assertFalse(FileParseUtil.isNormalFolder(MockKUtil.spyDir("mockDir")))
        }
        assertTrue(FileParseUtil.isNormalFolder(MockKUtil.spyDir("mockDir")))
    }

    @Test
    fun testParseJavaFiles(){
        // 文件不存在
        assertTrue(FileParseUtil.parseJavaFiles(File("mockPath")).isEmpty())

        // 存在 java 文件
        assertEquals(1,FileParseUtil.parseJavaFiles(spyBundle("mockPath","mockBundle","com.mock.module")).size)
    }

    @Test
    fun testIsParentBundle(){
        assertTrue(FileParseUtil.isParentBundle(MockKUtil.readFile("mockproj/mockmultimodule/app").absolutePath))

        assertFalse(FileParseUtil.isParentBundle(MockKUtil.readFile("mockproj/mockmultimodule/app/bootstrap").absolutePath))

        assertFalse(FileParseUtil.isParentBundle("mockNotExistsPath"))
    }

    @Test
    fun testIsPackage(){
        val file = MockKUtil.spyFile("mockFile")
        assertFalse(FileParseUtil.isPackage(file))

        val packageDir = MockKUtil.spyDir(StrUtil.join(FileUtil.FILE_SEPARATOR,"mockPath","src","main","java","com","mock","module"))
        assertTrue(FileParseUtil.isPackage(packageDir))

        val resourceDir = MockKUtil.spyDir(StrUtil.join(FileUtil.FILE_SEPARATOR,"mockPath","src","main","resources","mockDir"))
        assertFalse(FileParseUtil.isPackage(resourceDir))
    }

    @Test
    fun testIsFileInJavaRoot(){
        val dir = MockKUtil.spyDir("mockDir")
        assertFalse(FileParseUtil.isFileInJavaRoot(dir))

        val javaFile = MockKUtil.spyFile(StrUtil.join(FileUtil.FILE_SEPARATOR,"mockPath","src","main","java","com","mock","module","MockClass.java"))
        assertTrue(FileParseUtil.isFileInJavaRoot(javaFile))

        val resourceFile = MockKUtil.spyFile(StrUtil.join(FileUtil.FILE_SEPARATOR,"mockPath","src","main","resources","mockDir","mockFile.properties"))
        assertFalse(FileParseUtil.isFileInJavaRoot(resourceFile))

        val javaDir = MockKUtil.spyDir(StrUtil.join(FileUtil.FILE_SEPARATOR,"mockPath","src","main","java"))
        assertFalse(FileParseUtil.isFileInJavaRoot(javaDir))
    }
}
