import com.mongodb.spark.MongoSpark
import com.mongodb.spark.rdd.MongoRDD
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.bson.Document

object Spark3_MongoCluster {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR) //配置日志
    val session = SparkSession.builder()
      .master("local[*]")
      .appName("MongoSparkRDD")
      .config("spark.mongodb.input.uri", "mongodb://192.168.0.200:23000,192.168.0.201:23000,192.168.0.202:23000/mybike.bikes?readPreference=secondaryPreferred")   // 这里我配置的都是从节点，因为这里spark主要用于读取, 相当于  rs.slaveOk()  启用 备份节点可读
      .config("spark.mongodb.output.uri", "mongodb://192.168.0.200:23000,192.168.0.201:23000,192.168.0.202:23000/mybike.result")  // result是用于存分析结果的集合
      .getOrCreate()
    val sc= session.sparkContext     //上下文
    val docsRDD:MongoRDD[Document]=MongoSpark.load( sc )   //读取所有数据
    val result:Array[Document]=docsRDD.collect()     // 收集到客户端
    result.foreach(println)

    val df: DataFrame = MongoSpark.load(session)   //
    //2 创建视图
    df.createTempView("v_bikes")
    val bikes = session.sql("select * from v_bikes")
    MongoSpark.save(bikes )   //保存到 mongo 的result中

    sc.stop()
  }
}
