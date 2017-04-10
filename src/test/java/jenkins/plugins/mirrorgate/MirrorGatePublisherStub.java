package jenkins.plugins.mirrorgate;

public class MirrorGatePublisherStub extends MirrorGatePublisher {

    public MirrorGatePublisherStub(MirrorGateBuild buildStub) {
        super(buildStub);
    }

    public static class DescriptorImplStub extends MirrorGatePublisher.DescriptorImpl {

        private MirrorGateService mirrorGateService;

        @Override
        public synchronized void load() {
        }

        @Override
        public MirrorGateService getMirrorGateService(final String host) {
            return mirrorGateService;
        }

        public void setMirrorGateService(MirrorGateService mirrorGateService) {
            this.mirrorGateService = mirrorGateService;
        }
    }

    public static class MirrorGateBuildStub extends MirrorGateBuild {
        public MirrorGateBuildStub (boolean publishBuildStart ) {
            super(publishBuildStart);
        }
    }

}
