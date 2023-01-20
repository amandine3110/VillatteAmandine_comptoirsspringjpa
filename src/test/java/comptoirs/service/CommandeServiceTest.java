package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.LigneRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Commande;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CommandeServiceTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final String VILLE_PETIT_CLIENT = "Berlin";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");

    static final int NUMERO_COMMANDE_DEJA_LIVREE = 99999;
    static final int NUMERO_COMMANDE_PAS_LIVREE  = 99998;

    @Autowired
    private CommandeService service;
    @Autowired
    private ProduitRepository produitDao;
    @Autowired
    private CommandeRepository commandeDao;

    @Test
    void testCreerCommandePourGrosClient() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
        assertEquals(REMISE_POUR_GROS_CLIENT, commande.getRemise(),
            "Une remise de 15% doit être appliquée pour les gros clients");
    }

    @Test
    void testCreerCommandePourPetitClient() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertNotNull(commande.getNumero());
        assertEquals(BigDecimal.ZERO, commande.getRemise(),
            "Aucune remise ne doit être appliquée pour les petits clients");
    }

    @Test
    void testCreerCommandeInitialiseAdresseLivraison() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertEquals(VILLE_PETIT_CLIENT, commande.getAdresseLivraison().getVille(),
            "On doit recopier l'adresse du client dans l'adresse de livraison");
    }

    @Test
    void testDecrementationStock() {
        var produit = produitDao.findById(98).orElseThrow();
        int stockAvantDecrementation = produit.getUnitesEnStock();
        service.enregistreExpédition(NUMERO_COMMANDE_PAS_LIVREE);
        produit = produitDao.findById(98).orElseThrow();
        assertEquals(stockAvantDecrementation-20,produit.getUnitesEnStock(),"On doit décrémenter le stock de 20 unités");
    }

    /*
    @Test
    void testEnregistrementCommande() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        var produit = produitDao.findById(1).get();
        service.ajouterLigne(commande,produit,1);
        service.enregistreCommande(commande);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
    }
    */

    @Test
    void testEnregistrerLivraisonFixeDate() {
        Commande commande = commandeDao.findById(NUMERO_COMMANDE_PAS_LIVREE).orElseThrow();
        assertNull(commande.getEnvoyeele(),"La commande n'est pas encore livrée");
        commande = service.enregistreExpédition(NUMERO_COMMANDE_PAS_LIVREE);
        assertNotNull(commande.getEnvoyeele(),"La commande doit être livrée");
    }


}
