package com.myorg;

import java.util.Arrays;
import software.constructs.Construct;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.StageProps;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.ShellStep;
import software.amazon.awscdk.pipelines.StageDeployment;

public class MyPipelineStack extends Stack {
    public MyPipelineStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public MyPipelineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        final CodePipelineSource source = CodePipelineSource.gitHub("hoangnhps11609/ci-cd-aws-pipeline", "main");

        final ShellStep synth = ShellStep.Builder.create("Synth")
                            .input(source)
                            .commands(Arrays.asList("npm install -g aws-cdk", "cdk synth"))
                            .build(); 

        final CodePipeline pipeline = CodePipeline.Builder.create(this, "pipeline")
            .pipelineName("MyPipeline")
            .synth(synth)
            .build();


        final StageDeployment stage = 
            pipeline.addStage(new MyPipelineAppStage(this, "test", StageProps.builder()
                    .env(Environment.builder()
                            .account("030245542280")
                            .region("us-east-1")
                            .build())
                    .build()));

        
        stage.addPost(ShellStep.Builder.create("validate")
            .input(synth)
            .commands(Arrays.asList("node ./tests/validate.js"))
            .build());
    }
}