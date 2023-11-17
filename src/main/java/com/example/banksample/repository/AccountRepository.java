package com.example.banksample.repository;

import com.example.banksample.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

	// @Query("select distinct a from Account a join fetch a.user")
	@Query("SELECT ac FROM Account ac JOIN FETCH ac.user u WHERE ac.number = :number")
	Optional<Account> findByNumber(Long number);

	// select * from account where user_id = :id
	List<Account> findByUser_id(Long id);

}
