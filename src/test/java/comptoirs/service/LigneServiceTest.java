package comptoirs.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import comptoirs.dao.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolationException;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class LigneServiceTest {
    static final int NUMERO_COMMANDE_DEJA_LIVREE = 99999;
    static final int NUMERO_COMMANDE_PAS_LIVREE  = 99998;
    static final int NUMERO_COMMANDE_PAS_LIVREE_2  = 99997;
    static final int REFERENCE_PRODUIT_DISPONIBLE_1 = 93;
    static final int REFERENCE_PRODUIT_DISPONIBLE_2 = 94;
    static final int REFERENCE_PRODUIT_DISPONIBLE_3 = 95;
    static final int REFERENCE_PRODUIT_DISPONIBLE_4 = 96;
    static final int REFERENCE_PRODUIT_DISPONIBLE = 98;
    static final int REFERENCE_PRODUIT_INDISPONIBLE = 97;
    static final int UNITES_COMMANDEES_AVANT = 0;

    @Autowired
    LigneService service;
    @Autowired
    private ProduitRepository produitDao;

    @Test
    void onPeutAjouterDesLignesSiPasLivre() {
        var ligne = service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 1);
        assertNotNull(ligne.getId(),
        "La ligne doit être enregistrée, sa clé générée"); 
    }

    @Test
    void laQuantiteEstPositive() {
        assertThrows(ConstraintViolationException.class, 
            () -> service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 0),
            "La quantite d'une ligne doit être positive");
    }


    @Test
    void ajouterLigneSiProduitPasEnStock() {
        assertThrows(Exception.class,
                () -> service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_INDISPONIBLE, 1),
                "Le produit demandé n'est pas disponible");
    }


    @Test
    void testIncrementationQuantiteTot() {
        var produit = produitDao.findById(REFERENCE_PRODUIT_DISPONIBLE).orElseThrow();
        int unitesCommandeesAvantAjout = produit.getUnitesCommandees();
        service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE_2, REFERENCE_PRODUIT_DISPONIBLE,20);
        produit = produitDao.findById(REFERENCE_PRODUIT_DISPONIBLE).orElseThrow();
        assertEquals(unitesCommandeesAvantAjout+20,produit.getUnitesCommandees(),"20 produits d'id 98 ont été commandés en plus");
    }

}
