package twetailer.task;

import twetailer.dao.BaseOperations;
import twetailer.dao.ProposalOperations;

public class MockProposalProcessor extends ProposalProcessor {

    private static BaseOperations originalBaseOperations;
    private static ProposalOperations originalProposalOperations;

    public static BaseOperations injectMocks(BaseOperations mockBaseOperations) {
        originalBaseOperations = _baseOperations;
        _baseOperations = mockBaseOperations;
        return originalBaseOperations;
    }

    public static ProposalOperations injectMocks(ProposalOperations mockProposalOperations) {
        originalProposalOperations = proposalOperations;
        proposalOperations = mockProposalOperations;
        return originalProposalOperations;
    }

    public static void restoreOperation() {
        if (originalBaseOperations != null) {
            _baseOperations = originalBaseOperations;
        }
        if (originalProposalOperations != null) {
            proposalOperations = originalProposalOperations;
        }
    }
}
