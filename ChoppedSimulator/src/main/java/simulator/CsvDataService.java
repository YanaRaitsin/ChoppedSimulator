package simulator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.persistence.Query;

import data.Allergy;
import data.Foodtype;
import data.Ingredient;

public class CsvDataService {
	 private static Query query;
	 
	 private static boolean diabetes;
	 private static Vector<String> userPreference = new Vector<String>();
	 private static String userType;
	 private static Vector<String> userAllergies = new Vector<String>();
	 private static Map<String,String> ingredientsData = new LinkedHashMap<String, String>();
	 
	/*
	 * initIngredientDataList() - 
	 * The function save all the ingredients and the type of the ingredients to check later the ingredients with the data of the user (when the csvFileCreator will build the csv file).
	 */
	 @SuppressWarnings("unchecked")
	public static void initIngredientDataList() {
		JpaConnection.initEntityManager();
		List<Ingredient> saveIngredients = JpaConnection.getEntityManager().createQuery("Select i from Ingredient i").getResultList();
		List<Foodtype> saveFoodTypes = JpaConnection.getEntityManager().createQuery("Select f from Foodtype f").getResultList();
		Object IngredientId = null;
		Object TypeId = null;
		boolean checkContains = false;
		
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
		for(Ingredient i : saveIngredients) {
			for(Foodtype t : saveFoodTypes) {
				query = JpaConnection.getEntityManager().createNativeQuery("SELECT IngredientId it FROM ingredienttype it WHERE it.IngredientId = ?i AND it.TypeId= ?t");
				query.setParameter("i", i.getId());
				query.setParameter("t", t.getId());
				@SuppressWarnings("rawtypes")
				List foundIngredients = query.getResultList();
				if(!foundIngredients.isEmpty()) 
					IngredientId = foundIngredients.get(0);
				
				query = JpaConnection.getEntityManager().createNativeQuery("SELECT TypeId it FROM ingredienttype it WHERE it.IngredientId = ?i AND it.TypeId= ?t");
				query.setParameter("i", i.getId());
				query.setParameter("t", t.getId());
				@SuppressWarnings("rawtypes")
				List foundTypes = query.getResultList();
				if(!foundTypes.isEmpty())
					TypeId = foundTypes.get(0);
				
				/*
				 * Because we are using a map, we will check if there is more contains in the ingredient that
				 * we are current checking (adding another contains and not override the current one).
				 */
				if(!ingredientsData.isEmpty()) {
					for(Map.Entry<String, String> checkList : ingredientsData.entrySet()) {
		            	if(checkList.getKey().equals(i.getName())) {
		            		if(IngredientId!=null && TypeId!=null) {
		            		if(Integer.parseInt(IngredientId.toString()) == i.getId()) {
		            			if(Integer.parseInt(TypeId.toString()) == t.getId()) {
		            				checkContains = true;
		            				String currentContains = checkList.getValue();
		            				checkList.setValue(currentContains + "," + t.getName());
		            			}
		            		}
		            	}
		            }
		        }
			}
				
				if(IngredientId!=null && TypeId!=null && !checkContains) {
				if(Integer.parseInt(IngredientId.toString()) == i.getId()) {
					if(Integer.parseInt(TypeId.toString()) == t.getId()) {
						ingredientsData.put(i.getName(), t.getName());
						}
					}
				}
				IngredientId = null;
				TypeId = null;
				checkContains = false;
			}
		}
		JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception e) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
	}
	
	/*
	 * initUserData() - 
	 * The function save all the data that the user inserted in the registration.
	 * The function save in lists all the allergies of the user, preference and the type of the user.
	 */
	@SuppressWarnings("unchecked")
	public static void initUserData() {
		JpaConnection.initEntityManager();
		
		List<Object> AllergiesId;
		Object typeId = null;
		Object saveUserType = null;
		List<Allergy> saveAllergies;
		List<Ingredient> saveIngredients;
		Object savePreference = null;
		
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
			//when the user connect to the simulation, save the user from simulation service.
			//check if there is diabetes
			if(SimulationService.getUser().getDiabetes().equals("Yes"))
				diabetes = true;
			else
				diabetes = false;
		
			//find the type of the user by checking in joined table user-type.
			query = JpaConnection.getEntityManager().createNativeQuery("SELECT typeId ut FROM usertype ut WHERE ut.userId = ?userId");
			query.setParameter("userId",SimulationService.getUser().getId());
			@SuppressWarnings("rawtypes")
			List foundTypeByUserId = query.getResultList();
			if(!foundTypeByUserId.isEmpty())
				typeId = foundTypeByUserId.get(0);
		
			//find the type of the user by checking of the id type that we saved.
			query = JpaConnection.getEntityManager().createNativeQuery("SELECT type t FROM types t WHERE t.id = ?typeId");
			query.setParameter("typeId",typeId.toString());
			@SuppressWarnings("rawtypes")
			List foundTypeById = query.getResultList();
			if(!foundTypeById.isEmpty())
				saveUserType = foundTypeById.get(0);
			userType = saveUserType.toString();
		
			//Finding the allergies - first save all the id of the allergies in list
			query = JpaConnection.getEntityManager().createNativeQuery("SELECT allergiesId ua FROM userallergies ua WHERE ua.userId = ?userId");
			query.setParameter("userId",SimulationService.getUser().getId());
			AllergiesId = query.getResultList();
		
			//By the AllergiesId list of the user, we will save all the types of the allergies from database
			saveAllergies = JpaConnection.getEntityManager().createQuery("SELECT a FROM Allergy a").getResultList();
			for(Object allergyId : AllergiesId) {
				for(Allergy checkAllergies : saveAllergies) {
					if(Integer.parseInt(allergyId.toString()) == checkAllergies.getId())
						userAllergies.add(checkAllergies.getAllergy());
				}
			}
		
			//Saving all the ingredients that the user don't want to eat - preference
			saveIngredients = JpaConnection.getEntityManager().createQuery("Select i from Ingredient i").getResultList();
			for(Ingredient i : saveIngredients) {
				query = JpaConnection.getEntityManager().createNativeQuery("SELECT IngredientId up FROM userpreference up WHERE up.userId = ?userID AND up.IngredientId= ?ingredientID");
				query.setParameter("userID", SimulationService.getUser().getId());
				query.setParameter("ingredientID", i.getId());
				@SuppressWarnings("rawtypes")
				List foundPreference = query.getResultList();
				if(!foundPreference.isEmpty()) 
					savePreference = foundPreference.get(0);
			
				if(savePreference!=null) {
					if(Integer.parseInt(savePreference.toString()) == i.getId())
						if(!substituteForIngredients(i))
							userPreference.add(i.getName());
				}
			}
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception e) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
	}
	
	/*
	 * substituteForIngredients - 
	 * If there is substitute for the ingredient, the player can choose the recipe with the substitute and the original ingredient of the recipe.
	 * If not - the recipe will not show up in the simulation.
	 */
	public static boolean substituteForIngredients(Ingredient ingredient) {
		if(ingredient.getName().equals("eggs"))
			return true;
		else if(ingredient.getName().equals("sugar"))
			return true;
		else if(ingredient.getName().equals("milk"))
			return true;
		else if(ingredient.getName().equals("all purpose flour") || ingredient.getName().equals("white flour"))
			return true;
		else if(ingredient.getName().equals("vegetable oil"))
			return true;
		
		return false;
	}

	public static boolean isDiabetes() {
		return diabetes;
	}

	public static Vector<String> getUserPreference() {
		return userPreference;
	}

	public static String getUserType() {
		return userType;
	}

	public static Vector<String> getUserAllergies() {
		return userAllergies;
	}

	public static Map<String, String> getIngredientsData() {
		return ingredientsData;
	}
	
}
