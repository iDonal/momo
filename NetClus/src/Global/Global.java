package Global;

public class Global {

	  static public  final double lambda_P = 0.2; //prior knowledge
      static public  final double lambda_B = 0.8; //background
      static public  final double lambda_D = 0.9; //damping rate //0.1
      static public  final double lambda_C = 0.85; //smoothing rate
      
      static public String typeList[] = {"shop", "user",  "tag" };
      
      
      static public final String typeFileList[]= {"shop.txt", "user.txt", "tag.txt" };
      static public final String relFileList[] = { "user-shop.txt", "tag-shop.txt"};
      
      static public final String targetType = "shop";
      static public Integer targetTypeNum;

      static public  final String attributeTypeList[] = {"user",  "tag" };
      static public  final String relationList[] = {"user-shop",  "tag-shop" };
      static public final String BASEPATH ="D:/data/netclus/taobao/" ;
      static public final String separator = "\t";
      static public final int MAX_ITER_CLUSTER = 30;
      static public final int MAX_ITER_RANKING = 10;
      static public final int MIN_ITER_CLUSTER = 20;
}
