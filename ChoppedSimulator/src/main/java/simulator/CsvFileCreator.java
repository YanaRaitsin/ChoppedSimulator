package simulator;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;


public class CsvFileCreator {
	
private static List<String> nonVeganProducts = Arrays.asList("honey","meat","milk","poultry","whey","fish","gelatin","egg","dairy","lactose");

/*
 * createDatasetFile() - creates the csv to build later the decision tree by the file.
 * The function getting all the user data and the ingredient to check which ingredient the user can't eat.
 */
public static void createDatasetFile() {
	final String separator = ",";
    final String newLine = "\n";
    //CSV file header for attributes
    final String fileHeader = "ingrediant,vegan,lacto ovo,lacto,ovo,diabetes,sugar,lactose,dairy,nuts,egg,fish,soy,gluten,sesame,eat";
    String fileName = "resTree/dataset.csv";
    String checkAllergy = "sugar,lactose,dairy,nuts,egg,fish,soy,gluten,sesame";
    FileWriter fileWriter = null;
    boolean checkIfEdible=false;
    
    CsvDataService.initIngredientDataList();
    CsvDataService.initUserData();
    
    //Saving all the data from the database.
    Vector<String> userAllergies = CsvDataService.getUserAllergies();
    boolean diabetes = CsvDataService.isDiabetes();
    Map<String, String> ingrediantsData = CsvDataService.getIngredientsData();
    Vector<String> userPreference = CsvDataService.getUserPreference();
    String userType = CsvDataService.getUserType();
    
        try {
            fileWriter = new FileWriter(fileName);
            //Write the CSV file header
            fileWriter.append(fileHeader.toString());
            //Add a new line separator after the header
            fileWriter.append(newLine);
            Set<String> keys = ingrediantsData.keySet();
            Iterator<String> itr = keys.iterator();
            while(itr.hasNext()) {
            	String ingredientName = itr.next();
            	String contains = ingrediantsData.get(ingredientName);
            	fileWriter.append(ingredientName);
            	fileWriter.append(separator);
            	
            	//check preference of the user.
            	if(userType.equals("vegan")) {
            		fileWriter.append("yes");
            		fileWriter.append(separator);
            	}
            	else {
            		fileWriter.append("no");
            		fileWriter.append(separator);
            	}
            	if(userType.equals("lacto ovo")) {
            		fileWriter.append("yes");
            		fileWriter.append(separator);
            	}
            	else {
            		fileWriter.append("no");
            		fileWriter.append(separator);
            	}
            	if(userType.equals("lacto")) {
            		fileWriter.append("yes");
            		fileWriter.append(separator);
            	}
            	else {
            		fileWriter.append("no");
            		fileWriter.append(separator);
            	}
            	if(userType.equals("ovo")) {
            		fileWriter.append("yes");
            		fileWriter.append(separator);
            	}
            	else {
            		fileWriter.append("no");
            		fileWriter.append(separator);
            	}
            	
            	//check what the users allergies/intolerance
    			if(diabetes) {
    				fileWriter.append("yes");
    				fileWriter.append(separator);
    			}
    			else {
    				fileWriter.append("no");
    				fileWriter.append(separator);
    			}
    			
            	StringTokenizer stAllergy = new StringTokenizer(checkAllergy,",");
            	String current="";
            	while(stAllergy.hasMoreTokens()) {
            		current = stAllergy.nextToken();
            		boolean hasAllergy = false;
            		for(int i=0;i<userAllergies.size();i++) {
            			if(current.equals(userAllergies.get(i))) {
            				fileWriter.append("yes");
            				hasAllergy = true;
            			}
            		}
            		if(!hasAllergy)
            			fileWriter.append("no");
            		hasAllergy = false;
            		fileWriter.append(separator);
            	}
            	//check user type for the ingredients by what they contains
            	StringTokenizer stContains = new StringTokenizer(contains,",");
            	if(userType.equals("vegan")) {
            		while(stContains.hasMoreTokens()) {
            			current = stContains.nextToken();
            			for(String nonVegan : nonVeganProducts)
            				if(current.equals(nonVegan))
            					checkIfEdible=true;
            		}
            	}
            	else if(userType.equals("lacto ovo")){
            		while(stContains.hasMoreTokens()) {
            			current = stContains.nextToken();
            		if(current.equals("meat") || current.equals("fish") || current.equals("poultry"))
            			checkIfEdible=true;
            		}
            	}
            	else if(userType.equals("lacto")) {
            		while(stContains.hasMoreTokens()) {
            			current = stContains.nextToken();
            		if(current.equals("meat") || current.equals("fish") || current.equals("poultry") || current.equals("eggs"))
            			checkIfEdible=true;
            		}
            	}
            	else if(userType.equals("ovo")) {
            		while(stContains.hasMoreTokens()){
            			current = stContains.nextToken();
            		if(current.equals("meat") || current.equals("fish") || current.equals("poultry") || current.equals("dairy") || current.equals("milk") || current.equals("lactose"))
            			checkIfEdible=true;
            		}
            	}
            	
            	//check the preference of the user
            		for(int i=0;i<userPreference.size();i++)
            			if(ingredientName.equals(userPreference.get(i)))
            				checkIfEdible = true;
            	
            	//check allergies of the user by what the product contains
            	stContains = new StringTokenizer(contains,",");
            	while(stContains.hasMoreTokens()) {
            		current = stContains.nextToken();
            		for(int i=0;i<userAllergies.size();i++){
            			if(current.equals(userAllergies.get(i)))
            					checkIfEdible = true;
            			else if(diabetes) {
            				if(current.equals("sugar") || current.equals("sucrose") || current.equals("fructose"))
            					checkIfEdible = true;
            			}
            		}
            	}
            	
            	/*
            	 * If the current product is edible or not after checking the preference/type/allergies,
            	 * we will check if the boolean value is true or false.
            	 * true - not edible product.
            	 * false - edible product.
            	 */
            	if(checkIfEdible)
            		fileWriter.append("no");
            	else
            		fileWriter.append("yes");
            	fileWriter.append(newLine);
            	checkIfEdible = false;
         }
       } 
        catch (Exception e) {
            System.out.println("Error in CSVFileWriter");
            e.printStackTrace();
        } 
        finally { 
            try {
                fileWriter.flush();
                fileWriter.close();
            } 
            catch (IOException e) {
                System.out.println("Error while flushing or closing");
                e.printStackTrace();
            } 
        }
	}

}
