import { SwapsService } from './swaps.service.js';
import { ApiResponse } from '../../utils/apiResponse.js';
const swapsService = new SwapsService();
export class SwapsController {
    static async getMatches(req, res, next) {
        try {
            const matches = await swapsService.findReciprocalMatches(req.user.userId);
            return ApiResponse.success(res, matches, 'Reciprocal matches found');
        }
        catch (error) {
            next(error);
        }
    }
    static async createProposal(req, res, next) {
        try {
            const proposal = await swapsService.createProposal(req.user.userId, req.body);
            return ApiResponse.created(res, proposal, 'Swap proposal created successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async getProposals(req, res, next) {
        try {
            const { status, type } = req.query;
            const proposals = await swapsService.getProposals(req.user.userId, {
                status: status,
                type: type,
            });
            return ApiResponse.success(res, proposals, 'Swap proposals fetched');
        }
        catch (error) {
            next(error);
        }
    }
    static async updateStatus(req, res, next) {
        try {
            const id = req.params.id;
            const { status } = req.body;
            const updated = await swapsService.updateProposalStatus(req.user.userId, id, status);
            return ApiResponse.success(res, updated, `Swap proposal status updated to ${status}`);
        }
        catch (error) {
            next(error);
        }
    }
}
//# sourceMappingURL=swaps.controller.js.map