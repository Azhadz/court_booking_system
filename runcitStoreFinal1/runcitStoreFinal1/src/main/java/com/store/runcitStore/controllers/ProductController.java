package com.store.runcitStore.controllers;

import com.store.runcitStore.models.Product;
import com.store.runcitStore.models.ProductDto;
import com.store.runcitStore.services.ProductRepository;
import com.store.runcitStore.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import javax.swing.text.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/products")
public class ProductController {


    @Autowired
    private ProductRepository repo;
    @Autowired
    private ProductService productService;


    @GetMapping({"","/"})
    public String showProductList(@RequestParam(value = "searchString", required = false) String searchString, Model model){
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));

        if (searchString != null && !searchString.isEmpty()) {
            // Search products by name if a search query is present
            products = productService.searchByNameOrCategory(searchString);
        } else {
            // Otherwise, fetch all products
            products = productService.findAll();
        }
        model.addAttribute("products",products);
        model.addAttribute("searchString", searchString); // Pass the search string back to the view
        return "products/index";

    }

    @GetMapping("/create")
    public String showCreateProduct(Model model){
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto",productDto);
        return "products/create-product";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result){

        if(result.hasErrors()){
            return "products/create-product";
        }

        // Set the unique filename in the product (assuming Product entity has an imageFileName field)
        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());

        // Save the product in the repository
        repo.save(product);


        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String editProduct(Model model, @PathVariable int id) {
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product",product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());

            model.addAttribute("productDto",productDto);

        } catch (Exception e) {
            System.out.println("Error:"+e.getMessage());
            return "redirect:/products";
        }
        return "products/edit-product";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result) {
        if (result.hasErrors()) {
            return "products/edit-product";
        }

        try {
            Product product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());

            repo.save(product);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable int id) {
        // Retrieve the product to confirm existence (optional)
        Product product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));

        // Delete the product
        repo.delete(product);

        // Redirect to the product list
        return "redirect:/products";
    }

    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable int id, Model model) {
        Product product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
        model.addAttribute("product", product);
        return "products/view-product";
    }

    //search products by name or category
    @GetMapping("/search")
    public String searchProducts(@RequestParam("query") String query, Model model){
        List<Product> products = productService.searchByNameOrCategory(query);
        model.addAttribute("products", products);
        return "products/index"; //Display results on the same template
    }

    @Controller
    @RequestMapping("/calculator")
    public class CalculatorController {

        @GetMapping
        public String showCalculatorForm(Model model) {
            model.addAttribute("result", null); // Initialize with no result
            return "calculator";
        }

        @PostMapping
        public String calculate(@RequestParam("number1") double number1,
                                @RequestParam("number2") double number2,
                                @RequestParam("operation") String operation,
                                Model model) {
            double result = 0;

            switch (operation) {
                case "add":
                    result = number1 + number2;
                    break;
                case "subtract":
                    result = number1 - number2;
                    break;
                case "multiply":
                    result = number1 * number2;
                    break;
                case "divide":
                    if (number2 != 0) {
                        result = number1 / number2;
                    } else {
                        model.addAttribute("error", "Division by zero is not allowed!");
                        return "calculator";
                    }
                    break;
                default:
                    model.addAttribute("error", "Invalid operation!");
                    return "calculator";
            }

            model.addAttribute("result", result);
            return "calculator";
        }
    }

    @GetMapping("/calculate")
    public String showPriceCalculatorForm(Model model) {
        // Fetch all products for the dropdown
        List<Product> products = repo.findAll(Sort.by("name"));
        model.addAttribute("products", products);
        model.addAttribute("totalPrice", null);
        return "products/calculate-price";
    }

    @PostMapping("/calculate")
    public String calculateTotalPriceAndAverage(@RequestParam("productIds") List<Integer> productIds,
                                                @RequestParam("quantities") List<Integer> quantities,
                                                Model model) {
        if (productIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Product IDs and quantities must match in size.");
        }

        List<Product> selectedProducts = repo.findAllById(productIds);

        double totalPrice = 0.0;
        int totalQuantity = 0;
        List<Map<String, Object>> productDetails = new ArrayList<>();

        for (int i = 0; i < productIds.size(); i++) {
            int currentProductId = productIds.get(i);
            Product product = selectedProducts.stream()
                    .filter(p -> p.getId() == currentProductId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + currentProductId));

            int quantity = quantities.get(i);
            double productTotal = product.getPrice() * quantity;
            totalPrice += productTotal;
            totalQuantity += quantity;

            Map<String, Object> details = new HashMap<>();
            details.put("product", product);
            details.put("quantity", quantity);
            details.put("total", productTotal);
            productDetails.add(details);
        }

        // Calculate the average price
        double averagePrice = totalQuantity > 0 ? totalPrice / totalQuantity : 0.0;

        // Add attributes for rendering in the view
        model.addAttribute("productDetails", productDetails);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("averagePrice", averagePrice);

        return "products/calculate-price";
    }




    @PreAuthorize("hasRole('USER')")
    @GetMapping("/products")
    public String productsPage() {
        return "products/index";
    }


}
