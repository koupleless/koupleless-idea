modulename=$1
archetypeGroupId=$2
archetypeArtifactId=$3
archetypeVersion=$4
modulePackage=$5
moduleGroupId=$6
moduleArtifactId=$7

if [ "$modulename" = "" ]; then
  echo '请输入模块名称 sh initArkModule.sh {moduleName} {archetypeGroupId} {archetypeArtifactId} {archetypeVersion} {modulePackage} {moduleGroupId} {moduleArtifactId}'
  exit 0
fi
basedir=$(pwd)
mkdir -p arkmodule && cd arkmodule

if [ -d "$basedir/arkmodule/$modulename" ]; then
  rm -rf "$basedir/arkmodule/$modulename"
  echo "清理原目录：$basedir/arkmodule/$modulename"
fi

if [ -d "$basedir/arkmodule/$moduleArtifactId" ]; then
  rm -rf "$basedir/arkmodule/$moduleArtifactId"
  echo "清理临时目录：$basedir/arkmodule/$moduleArtifactId"
fi

baseAppName=`basename $(pwd)`
echo "模块 $modulename 临时目录 $moduleArtifactId 开始生成中..."
mvn org.apache.maven.plugins:maven-archetype-plugin:2.2:generate \
    -DarchetypeRepository=http://mvn.test.alipay.net:8080/artifactory/repo \
    -DgroupId=$moduleGroupId \
    -DartifactId=$moduleArtifactId \
    -DbaseAppName=$baseAppName \
    -DarchetypeGroupId=${archetypeGroupId} \
    -DarchetypeArtifactId=${archetypeArtifactId} \
    -DarchetypeVersion=${archetypeVersion} \
    -Dpackage=${modulePackage} \
    -DinteractiveMode=false

if [ $? != 0 ]; then
  echo "模块 $modulename 临时目录 $moduleArtifactId 创建失败"
  rm -rf "$basedir/arkmodule/$moduleArtifactId"
  exit 1
fi

if [ "$moduleArtifactId" != "$modulename" ]; then
    mv "$basedir/arkmodule/$moduleArtifactId" "$basedir/arkmodule/$modulename"
fi

if [ $? != 0 ]; then
  echo "模块 $modulename 目录创建失败"
  rm -rf "$basedir/arkmodule/$modulename"
  exit 1
fi
echo "模块 $modulename 目录创建成功"

###替换parent
cd "$basedir"

echo "模块 $modulename 开始替换parent"
artifactId=`mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.artifactId -q -DforceStdout`
groupId=`mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.groupId -q -DforceStdout`
version=`mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout`
realParent="\\
    <parent>\\
        <groupId>$groupId</groupId>\\
        <artifactId>$artifactId</artifactId>\\
        <version>$version</version>\\
        <relativePath>../../pom.xml</relativePath>\\
    </parent>\\
"

sed -i "" '/<parent>/, /<\/parent>/c\
'"$realParent"' \
' ./arkmodule/$modulename/pom.xml
echo "模块 $modulename 替换parent结束"

sed -i "" -e "/<\/modules>/i\\
        <module>arkmodule/$modulename</module>
" ./pom.xml
echo "基座根pom的modules中已增加模块:  <module>arkmodule/$modulename</module>"
