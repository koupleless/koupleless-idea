modulename=$1
baseAppName=$2
archetypeGroupId=$3
archetypeArtifactId=$4
archetypeVersion=$5
moduleGroupId=$6
moduleArtifactId=$7
modulePackage=$8
moduleLocation=$9

if [ "$modulename" = "" ]; then
  echo '请输入模块名称 sh initIndependentModule.sh {moduleName} {baseAppName} {archetypeGroupId} {archetypeArtifactId} {archetypeVersion} {moduleGroupId} {moduleArtifactId} {modulePackage} {modulePath}'
  exit 0
fi

mkdir -p "$moduleLocation" && cd "$moduleLocation"

if [ -d "$moduleLocation/$modulename" ]; then
  rm -rf "$moduleLocation/$modulename"
  echo "清理原目录：$moduleLocation/$modulename"
fi

echo "模块 $modulename 目录开始生成中..."

mvn org.apache.maven.plugins:maven-archetype-plugin:2.2:generate \
    -DarchetypeRepository=http://mvn.test.alipay.net:8080/artifactory/repo \
    -DgroupId=$moduleGroupId \
    -DartifactId=$moduleArtifactId \
    -Dpackage=${modulePackage} \
    -DbaseAppName=$baseAppName \
    -DarchetypeGroupId=${archetypeGroupId} \
    -DarchetypeArtifactId=${archetypeArtifactId} \
    -DarchetypeVersion=${archetypeVersion} \
    -DinteractiveMode=false

if [ $? != 0 ]; then
  echo "模块 $modulename 目录创建失败"
  rm -rf "$moduleLocation/$modulename"
  exit 1
fi
echo "模块 $modulename 目录创建成功"
