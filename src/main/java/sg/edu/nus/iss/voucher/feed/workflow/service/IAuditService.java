package sg.edu.nus.iss.voucher.feed.workflow.service;

import sg.edu.nus.iss.voucher.feed.workflow.dto.AuditDTO;

public interface IAuditService {
	void sendMessage(AuditDTO autAuditDTO);
 
}
