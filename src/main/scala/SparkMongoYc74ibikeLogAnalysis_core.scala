import com.mongodb.spark.MongoSpark
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}


object SparkMongoYc74ibikeLogAnalysis_core {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR) //配置日志
    val conf = new SparkConf()
      .setAppName("MongoSparkRDD")
      .setMaster("local[*]")
      .set("spark.mongodb.input.uri", "mongodb://a:a@192.168.0.200:27017/yc74ibike.logs")
      .set("spark.mongodb.output.uri", "mongodb://a:a@192.168.0.200:27017/yc74ibike.result")
    //创建sparkcontext(RDD,SparkCore)
    val sc = new SparkContext(conf)
    val docsRDD = MongoSpark.load(sc)

    //先缓存原始数据
    docsRDD.cache()   //    persist(       )

    println("原始数据:")
    val r = docsRDD.collect()
    println(r.toBuffer)

    //先过滤，filteredRDD，缓存（cache）
    //计算pv
    val pv = docsRDD.count()   // 动作操作
    //计算uv
    val uv = docsRDD.map(doc => {
      doc.getString("openid")
    }).distinct().count()
    println("pv: " + pv + " uv: " + uv)

    sc.stop()
  }
}
