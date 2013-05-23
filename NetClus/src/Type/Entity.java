package Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Entity {

	public boolean contains(long ID){
		return ID2Index.containsKey(ID);
	}
	public boolean contains(String name){
		return name2Index.containsKey(name);
	}
	
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public Map<Long, Integer> getID2Index() {
		return ID2Index;
	}

	public Map<Integer, Long> getIndex2ID() {
		return Index2ID;
	}
	
	public ArrayList<String> getNameList() {
		return nameList;
	}
	
	public int size() {
		return index;
	}
	public int getIndexByID(long ID){
		return ID2Index.get(ID);
	}
	public long getIDByIndex(int index){
		return Index2ID.get(index);
	}
	public String getNameByIndex(int index) {
		return nameList.get(index);
	}
	public String getNameByID(long ID) {
		return nameList.get(this.getIndexByID(ID));
	}
	public int getIndexByname(String name) {
		return name2Index.get(name);
	}
	public Entity() {
		// TODO Auto-generated constructor stub
		ID2Index = new HashMap<Long, Integer>();
		Index2ID = new HashMap<Integer, Long>();
		nameList = new ArrayList<String>();
		name2Index = new HashMap<String, Integer>();
		index = 0;
	}
	public void set(long ID , String name) {
		
		ID2Index.put(ID, index);
		Index2ID.put(index, ID);
		nameList.add(name);
		index++;
	}
	public void set(String name){
		nameList.add(name);
		name2Index.put(name, index);
		index++;
	}
	public void set(long ID) {
		ID2Index.put(ID, index);
		Index2ID.put(index, ID);
		index++;
	}
	private String typeName ="";
	private Map<Long, Integer>ID2Index =null;
	private Map<Integer, Long>Index2ID = null;
	private ArrayList<String> nameList = null;
	private Map<String, Integer> name2Index = null; //just for tag
	private int index;
	
	
}
