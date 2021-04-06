package com.eress.apimaven.repo.board;

import com.eress.apimaven.entity.board.Board;
import com.eress.apimaven.entity.board.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostJpaRepo extends JpaRepository<Post, Long> {
    List<Post> findByBoard(Board board);
    List<Post> findByBoardOrderByPostIdDesc(Board board);
}
