package com.ecommerce.micrommerce.web.controller;

import com.ecommerce.micrommerce.web.dao.ProductDao;
import com.ecommerce.micrommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.micrommerce.web.exceptions.ProduitIntrouvableException;
import com.ecommerce.micrommerce.web.model.Product;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

@Api( "API pour les opérations CRUD sur les produits.")
@RestController
public class ProductController {

    private final ProductDao productDao;

    public ProductController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {
        productDao.deleteById(id);
    }

    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {
        productDao.save(product);
    }

    //Récupérer la liste des produits
    @GetMapping("/Produits")
    public List<Product> listeProduits() {
        return productDao.findAll();
    }

    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) {
        Product produit = productDao.findById(id);
        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");
        return produit;
    }

    @GetMapping(value = "/test/produits/{prixLimit}")
    public List<Product> testeDeRequetes(@PathVariable int prixLimit) {
        return productDao.findByPrixGreaterThan(prixLimit);
    }

    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock et calcul la marge faite sur le produit")
    @GetMapping(value = "/AdminProduits")
    public HashMap<String, Integer> calculerMargeProduit() {
        Iterable<Product> products = productDao.findAll();
        HashMap<String, Integer> hashMapProduct = new HashMap<>();

        products.forEach(product -> hashMapProduct.put(product.toString(), product.getPrix()-product.getPrixAchat()));

        return hashMapProduct;
    }

    @GetMapping(value = "/TriProduits")
    public List<Product> trierProduitsParOrdreAlphabetique() {
        return productDao.findAllByOrderByNom();
    }

    @PostMapping(value = "/Produits")
    public ResponseEntity<Product> ajouterProduit(@RequestBody @Valid Product product) {
        if (product.getPrix() <= 0) throw new ProduitGratuitException("Un produit ne peut pas être gratuit.");
        Product productAdded = productDao.save(product);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
}