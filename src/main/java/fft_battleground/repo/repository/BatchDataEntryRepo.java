package fft_battleground.repo.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.util.BatchDataEntryType;

public interface BatchDataEntryRepo extends JpaRepository<BatchDataEntry, Long> {

	@Query("SELECT batchDataEntry FROM BatchDataEntry batchDataEntry WHERE batchDataEntry.batchDataEntryType = :batchDataEntryType ORDER BY batchDataEntry.updateComplete DESC")
	public List<BatchDataEntry> getBatchDataEntryForBatchEntryType(@Param("batchDataEntryType") BatchDataEntryType batchDataEntryType, Pageable pageable);
	
	public default BatchDataEntry getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType batchDataEntryType) {
		List<BatchDataEntry> batchDataEntry= this.getBatchDataEntryForBatchEntryType(batchDataEntryType, PageRequest.of(0,1));
		BatchDataEntry result = null;
		if(batchDataEntry != null && batchDataEntry.size() > 0) {
			result = batchDataEntry.get(0);
		}
		return result;
	}
}
