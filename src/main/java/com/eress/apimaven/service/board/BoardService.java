package com.eress.apimaven.service.board;

import com.eress.apimaven.advice.exception.CNotOwnerException;
import com.eress.apimaven.advice.exception.CResourceNotExistException;
import com.eress.apimaven.advice.exception.CUserNotFoundException;
import com.eress.apimaven.common.CacheKey;
import com.eress.apimaven.entity.User;
import com.eress.apimaven.entity.board.Board;
import com.eress.apimaven.entity.board.Post;
import com.eress.apimaven.model.board.ParamsPost;
import com.eress.apimaven.repo.UserJpaRepo;
import com.eress.apimaven.repo.board.BoardJpaRepo;
import com.eress.apimaven.repo.board.PostJpaRepo;
import com.eress.apimaven.service.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardJpaRepo boardJpaRepo;
    private final PostJpaRepo postJpaRepo;
    private final UserJpaRepo userJpaRepo;
    private final CacheService cacheService;

    public Board insertBoard(String boardName) {
        return boardJpaRepo.save(Board.builder().name(boardName).build());
    }

    // 게시판 이름으로 게시판을 조회. 없을경우 CResourceNotExistException 처리
//    public Board findBoard(String boardName) {
//        return Optional.ofNullable(boardJpaRepo.findByName(boardName)).orElseThrow(CResourceNotExistException::new);
//    }
    @Cacheable(value = CacheKey.BOARD, key = "#boardName", unless = "#result == null")
    public Board findBoard(String boardName) {
        return Optional.ofNullable(boardJpaRepo.findByName(boardName)).orElseThrow(CResourceNotExistException::new);
    }

    // 게시판 이름으로 게시글 리스트 조회.
//    public List<Post> findPosts(String boardName) {
//        return postJpaRepo.findByBoard(findBoard(boardName));
//    }
    @Cacheable(value = CacheKey.POSTS, key = "#boardName", unless = "#result == null")
    public List<Post> findPosts(String boardName) {
        return postJpaRepo.findByBoardOrderByPostIdDesc(findBoard(boardName));
    }

    // 게시글ID로 게시글 단건 조회. 없을경우 CResourceNotExistException 처리
//    public Post getPost(long postId) {
//        return postJpaRepo.findById(postId).orElseThrow(CResourceNotExistException::new);
//    }
    @Cacheable(value = CacheKey.POST, key = "#postId", unless = "#result == null")
    public Post getPost(long postId) {
        return postJpaRepo.findById(postId).orElseThrow(CResourceNotExistException::new);
    }

    // 게시글을 등록합니다. 게시글의 회원UID가 조회되지 않으면 CUserNotFoundException 처리합니다.
//    public Post writePost(String uid, String boardName, ParamsPost paramsPost) {
//        Board board = findBoard(boardName);
//        Post post = new Post(userJpaRepo.findByUid(uid).orElseThrow(CUserNotFoundException::new), board, paramsPost.getAuthor(), paramsPost.getTitle(), paramsPost.getContent());
//        return postJpaRepo.save(post);
//    }
    @CacheEvict(value = CacheKey.POSTS, key = "#boardName")
    public Post writePost(String uid, String boardName, ParamsPost paramsPost) {
        Board board = findBoard(boardName);
        Post post = new Post(userJpaRepo.findByUid(uid).orElseThrow(CUserNotFoundException::new), board, paramsPost.getAuthor(), paramsPost.getTitle(), paramsPost.getContent());
        return postJpaRepo.save(post);
    }

    // 게시글을 수정합니다. 게시글 등록자와 로그인 회원정보가 틀리면 CNotOwnerException 처리합니다.
//    public Post updatePost(long postId, String uid, ParamsPost paramsPost) {
//        Post post = getPost(postId);
//        User user = post.getUser();
//        if (!uid.equals(user.getUid()))
//            throw new CNotOwnerException();
//        // 영속성 컨텍스트의 변경감지(dirty checking) 기능에 의해 조회한 Post내용을 변경만 해도 Update쿼리가 실행됩니다.
//        post.setUpdate(paramsPost.getAuthor(), paramsPost.getTitle(), paramsPost.getContent());
//        return post;
//    }
//    @CachePut(value = CacheKey.POST, key = "#postId") 갱신된 정보만 캐시할경우에만 사용!
    public Post updatePost(long postId, String uid, ParamsPost paramsPost) {
        Post post = getPost(postId);
        User user = post.getUser();
        if (!uid.equals(user.getUid()))
            throw new CNotOwnerException();
        // 영속성 컨텍스트의 변경감지(dirty checking) 기능에 의해 조회한 Post내용을 변경만 해도 Update쿼리가 실행됩니다.
        post.setUpdate(paramsPost.getAuthor(), paramsPost.getTitle(), paramsPost.getContent());
        cacheService.deleteBoardCache(post.getPostId(), post.getBoard().getName());
        return post;
    }

    // 게시글을 삭제합니다. 게시글 등록자와 로그인 회원정보가 틀리면 CNotOwnerException 처리합니다.
//    public boolean deletePost(long postId, String uid) {
//        Post post = getPost(postId);
//        User user = post.getUser();
//        if (!uid.equals(user.getUid()))
//            throw new CNotOwnerException();
//        postJpaRepo.delete(post);
//        return true;
//    }
    public boolean deletePost(long postId, String uid) {
        Post post = getPost(postId);
        User user = post.getUser();
        if (!uid.equals(user.getUid()))
            throw new CNotOwnerException();
        postJpaRepo.delete(post);
        cacheService.deleteBoardCache(post.getPostId(), post.getBoard().getName());
        return true;
    }
}
