package com.example.anonserver.api;

import com.example.anonserver.api.models.comments.CommentAdminResponse;
import com.example.anonserver.api.models.comments.CommentAdminSelfResponse;
import com.example.anonserver.api.models.comments.CommentBaseResponse;
import com.example.anonserver.api.models.comments.CommentBaseSelfResponse;
import com.example.anonserver.api.models.edit.EditCommentRequest;
import com.example.anonserver.api.models.edit.EditPostRequest;
import com.example.anonserver.api.models.posts.PostAdminResponse;
import com.example.anonserver.api.models.posts.PostAdminSelfResponse;
import com.example.anonserver.api.models.posts.PostBaseResponse;
import com.example.anonserver.api.models.posts.PostBaseSelfResponse;
import com.example.anonserver.domain.models.CommentModel;
import com.example.anonserver.domain.models.PostModel;
import com.example.anonserver.domain.models.Role;
import com.example.anonserver.domain.models.UserModel;
import com.example.anonserver.repositories.CommentRepository;
import com.example.anonserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("api/comments")
public class CommentController {


    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;


    @GetMapping("getById")
    public ResponseEntity<CommentBaseResponse> getById(@RequestParam("id") long id){
        if(commentRepository.existsById(id)){
            CommentModel c = commentRepository.findById(id).get();
            if (!c.isBanned() || !c.isDeleted()){
                return ResponseEntity.ok(new CommentBaseResponse(c.getId(), c.getText(), c.getImagesUrls(), c.getFilesUrls()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("getAdmById")
    public ResponseEntity<CommentAdminResponse> getAdmById(@RequestParam("id") long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.getAuthorities().contains(Role.ADMIN)) {
            if(commentRepository.existsById(id)){
                CommentModel c = commentRepository.findById(id).get();
                return ResponseEntity.ok(new CommentAdminResponse(c.getId(), c.getAuthorId(), c.getText(), c.getImagesUrls(), c.getFilesUrls(), c.isBanned(), c.isDeleted()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    @GetMapping("getSelfById")
    public ResponseEntity<CommentBaseSelfResponse> getSelfById(@RequestParam("id") long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(userRepository.existsByUsername(auth.getName()) && commentRepository.existsById(id)) {
            CommentModel c = commentRepository.findById(id).get();
            UserModel u = userRepository.findByUsername(auth.getName()).get();
            if(c.getAuthorId() == u.getId()){
                if(!c.isDeleted()) {
                    return ResponseEntity.ok(new CommentBaseSelfResponse(c.getId(), c.getAuthorId(), c.getText(), c.getImagesUrls(), c.getFilesUrls(), c.isBanned()));
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @GetMapping("getAdmSelfById")
    public ResponseEntity<CommentAdminSelfResponse> getAdmSelfById(@RequestParam("id") long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(userRepository.existsByUsername(auth.getName()) && commentRepository.existsById(id)) {
            CommentModel p = commentRepository.findById(id).get();
            UserModel u = userRepository.findByUsername(auth.getName()).get();
            if(p.getAuthorId() == u.getId() && auth.getAuthorities().contains(Role.ADMIN)){
                return ResponseEntity.ok(new CommentAdminSelfResponse(p.getId(), p.getAuthorId(), p.getText(), p.getImagesUrls(), p.getFilesUrls(), p.isBanned(), p.isDeleted()));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("delete")
    public ResponseEntity delete(@RequestParam("id") long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(commentRepository.existsById(id) || userRepository.existsByUsername(auth.getName())) {
            UserModel u = userRepository.findByUsername(auth.getName()).get();
            CommentModel c = commentRepository.findById(id).get();
            if (c.getAuthorId() == u.getId()) {
                commentRepository.save(new CommentModel(c.getId(), c.getAuthorId(), c.getText(), c.getImagesUrls(), c.getFilesUrls(), c.getUploadTime(), c.isEdited(), c.isBanned(), true));
                return ResponseEntity.ok(null);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @PostMapping("ban")
    public ResponseEntity ban(@RequestParam("id") long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(commentRepository.existsById(id) || userRepository.existsByUsername(auth.getName())) {
            UserModel u = userRepository.findByUsername(auth.getName()).get();
            CommentModel c = commentRepository.findById(id).get();
            if (auth.getAuthorities().contains(Role.ADMIN)) {
                commentRepository.save(new CommentModel(c.getId(), c.getAuthorId(), c.getText(), c.getImagesUrls(), c.getFilesUrls(), c.getUploadTime(), c.isEdited(), true, c.isDeleted()));
                return ResponseEntity.ok(null);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @PostMapping("unban")
    public ResponseEntity unban(@RequestParam("id") long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(commentRepository.existsById(id) || userRepository.existsByUsername(auth.getName())) {
            UserModel u = userRepository.findByUsername(auth.getName()).get();
            CommentModel c = commentRepository.findById(id).get();
            if (auth.getAuthorities().contains(Role.ADMIN)) {
                commentRepository.save(new CommentModel(c.getId(), c.getAuthorId(), c.getText(), c.getImagesUrls(), c.getFilesUrls(), c.getUploadTime(), c.isEdited(), false, c.isDeleted()));
                return ResponseEntity.ok(null);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @PostMapping("edit")
    public ResponseEntity edit(@RequestParam("id") long id, @RequestBody EditCommentRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(commentRepository.existsById(id) || userRepository.existsByUsername(auth.getName())) {
            UserModel u = userRepository.findByUsername(auth.getName()).get();
            CommentModel c = commentRepository.findById(id).get();
            if (c.getAuthorId() == u.getId()) {
                commentRepository.save(new CommentModel(c.getId(), c.getAuthorId(), request.getText(), request.getImagesUrls(), request.getFilesUrls(), c.getUploadTime(), true, c.isBanned(), c.isDeleted()));
                return ResponseEntity.ok(null);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @PostMapping("create")
    public ResponseEntity create(@RequestBody EditCommentRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserModel u = userRepository.findByUsername(auth.getName()).get();
        CommentModel p = new CommentModel(0, u.getId(), request.getText(), request.getImagesUrls(), request.getFilesUrls(), System.currentTimeMillis(), false, false, false);
        commentRepository.save(p);
        return ResponseEntity.ok(null);
    }

}
