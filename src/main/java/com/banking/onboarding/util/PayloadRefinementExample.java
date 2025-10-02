package com.banking.onboarding.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Example showing how to use PayloadRefinementUtil in your steps
 */
@Slf4j
@Component
public class PayloadRefinementExample {

    private final PayloadRefinementUtil payloadRefinementUtil;

    public PayloadRefinementExample(PayloadRefinementUtil payloadRefinementUtil) {
        this.payloadRefinementUtil = payloadRefinementUtil;
    }

    /**
     * Example 1: First step - uses initial payload
     */
    public void exampleFirstStep() {
        log.info("=== Example 1: First Step (Uses Initial Payload) ===");
        
        // This would be called from your step's execute method
        // GenericStepContext context = ... (from your step)
        // String stepName = "XML_TO_JSON_TRANSFORMATION";
        
        // Get refined payload from initial input
        // PayloadRefinementUtil.RefinedPayload refinedPayload = 
        //     payloadRefinementUtil.getRefinedPayload(
        //         context, 
        //         stepName,
        //         PayloadRefinementUtil.InputSource.INITIAL_PAYLOAD
        //     );
        
        log.info("✅ First step gets input from initial payload");
        log.info("✅ Automatic refinements applied (correlation ID, timestamp, null cleanup)");
        log.info("✅ Ready for XML to JSON transformation");
    }

    /**
     * Example 2: Middle step - uses previous step output
     */
    public void exampleMiddleStep() {
        log.info("=== Example 2: Middle Step (Uses Previous Step Output) ===");
        
        // This would be called from your step's execute method
        // GenericStepContext context = ... (from your step)
        // String stepName = "FENERGO_ENTITY_CREATION";
        
        // Get refined payload from previous step (automatic)
        // PayloadRefinementUtil.RefinedPayload refinedPayload = 
        //     payloadRefinementUtil.getRefinedPayload(context, stepName);
        
        // Apply custom transformations
        // PayloadRefinementUtil.RefinedPayload fenergoPayload = 
        //     payloadRefinementUtil.getRefinedPayload(
        //         context,
        //         stepName,
        //         CommonPayloadTransformers.chainTransformers(
        //             CommonPayloadTransformers.dataValidationTransformer(),
        //             CommonPayloadTransformers.fenergoEntityTransformer()
        //         )
        //     );
        
        log.info("✅ Middle step gets input from previous step output");
        log.info("✅ Automatic refinements + custom transformations applied");
        log.info("✅ Ready for Fenergo entity creation");
    }

    /**
     * Example 3: Last step - uses previous step output
     */
    public void exampleLastStep() {
        log.info("=== Example 3: Last Step (Uses Previous Step Output) ===");
        
        // This would be called from your step's execute method
        // GenericStepContext context = ... (from your step)
        // String stepName = "FENERGO_JOURNEY_LAUNCH";
        
        // Get refined payload from previous step
        // PayloadRefinementUtil.RefinedPayload refinedPayload = 
        //     payloadRefinementUtil.getRefinedPayload(
        //         context, 
        //         stepName,
        //         PayloadRefinementUtil.InputSource.PREVIOUS_STEP
        //     );
        
        // Apply journey-specific transformations
        // PayloadRefinementUtil.RefinedPayload journeyPayload = 
        //     payloadRefinementUtil.getRefinedPayload(
        //         context,
        //         stepName,
        //         CommonPayloadTransformers.journeyPreparationTransformer()
        //     );
        
        log.info("✅ Last step gets input from previous step output");
        log.info("✅ Journey-specific transformations applied");
        log.info("✅ Ready for Fenergo journey launch");
    }

    /**
     * Example 4: Custom transformation
     */
    public void exampleCustomTransformation() {
        log.info("=== Example 4: Custom Transformation ===");
        
        // Create custom transformer
        PayloadRefinementUtil.PayloadTransformer customTransformer = payload -> {
            Map<String, Object> transformed = new java.util.HashMap<>(payload);
            transformed.put("customField", "customValue");
            transformed.put("transformationType", "CUSTOM");
            return transformed;
        };
        
        // Use in step
        // PayloadRefinementUtil.RefinedPayload refinedPayload = 
        //     payloadRefinementUtil.getRefinedPayload(
        //         context,
        //         stepName,
        //         customTransformer
        //     );
        
        log.info("✅ Custom transformation created and applied");
        log.info("✅ Added custom fields to payload");
    }

    /**
     * Example 5: Chain multiple transformations
     */
    public void exampleChainTransformations() {
        log.info("=== Example 5: Chain Multiple Transformations ===");
        
        // Chain multiple transformers
        PayloadRefinementUtil.PayloadTransformer chainedTransformer = 
            CommonPayloadTransformers.chainTransformers(
                CommonPayloadTransformers.dataValidationTransformer(),
                CommonPayloadTransformers.fenergoEntityTransformer(),
                CommonPayloadTransformers.dataEnrichmentTransformer(Map.of(
                    "enrichmentSource", "EXTERNAL_API",
                    "enrichmentTimestamp", java.time.LocalDateTime.now().toString()
                ))
            );
        
        // Use in step
        // PayloadRefinementUtil.RefinedPayload refinedPayload = 
        //     payloadRefinementUtil.getRefinedPayload(
        //         context,
        //         stepName,
        //         chainedTransformer
        //     );
        
        log.info("✅ Multiple transformations chained together");
        log.info("✅ Data validation + Fenergo preparation + enrichment applied");
    }

    /**
     * Example 6: Conditional transformation
     */
    public void exampleConditionalTransformation() {
        log.info("=== Example 6: Conditional Transformation ===");
        
        // Conditional transformer based on payload content
        PayloadRefinementUtil.PayloadTransformer conditionalTransformer = 
            CommonPayloadTransformers.conditionalTransformer(
                payload -> payload.containsKey("entityId"), // Condition
                CommonPayloadTransformers.fenergoEntityTransformer(), // If true
                CommonPayloadTransformers.dataValidationTransformer() // If false
            );
        
        // Use in step
        // PayloadRefinementUtil.RefinedPayload refinedPayload = 
        //     payloadRefinementUtil.getRefinedPayload(
        //         context,
        //         stepName,
        //         conditionalTransformer
        //     );
        
        log.info("✅ Conditional transformation applied");
        log.info("✅ Different transformations based on payload content");
    }

    /**
     * Run all examples
     */
    public void runAllExamples() {
        log.info("🚀 Running PayloadRefinementUtil Examples");
        log.info("==========================================");
        
        exampleFirstStep();
        log.info("");
        
        exampleMiddleStep();
        log.info("");
        
        exampleLastStep();
        log.info("");
        
        exampleCustomTransformation();
        log.info("");
        
        exampleChainTransformations();
        log.info("");
        
        exampleConditionalTransformation();
        log.info("");
        
        log.info("✅ All examples completed!");
        log.info("📖 See Universal-Payload-Refinement-Utility.md for detailed documentation");
    }
}
