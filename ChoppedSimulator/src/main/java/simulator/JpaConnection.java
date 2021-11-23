package simulator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JpaConnection {

	 private static EntityManagerFactory entityFactory = Persistence.createEntityManagerFactory("Chopped-Simulator_JPA");
	 private static EntityManager entityManager = entityFactory.createEntityManager();
	 
	public static void initEntityManager() {
		entityManager = entityFactory.createEntityManager();
	}
	
	public static void closeEntityManager() {
		entityManager.close();
	}

	public static EntityManager getEntityManager() {
		return entityManager;
	}
}
