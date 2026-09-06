package com.myeshopping.pickingservice.repository;
import com.myeshopping.pickingservice.entity.PickingTask; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface PickingTaskRepository extends JpaRepository<PickingTask,Long>{ Optional<PickingTask> findByOrderId(Long orderId); }
