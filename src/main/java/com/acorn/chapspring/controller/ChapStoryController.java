package com.acorn.chapspring.controller;

import com.acorn.chapspring.dto.ChapDealDto;
import com.acorn.chapspring.dto.ChapstoryimgsDto;
import com.acorn.chapspring.dto.ChapstorysDto;
import com.acorn.chapspring.dto.UserDto;
import com.acorn.chapspring.service.ChapStoryService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Controller //< @Component 요청과 응답을 처리 가능
@RequestMapping("/chapstory")
@Log4j2 //log 필드로 로그남길 수 있다.(파일로 저장 가능[유지기간,성질])
public class ChapStoryController {
    private ChapStoryService chapStoryService;
    @Value("${img.upload.path}")
    private String uploadPath; //등록 (프로젝트위치+/static/public/img/chapstory
    @Value("${static.path}")
    private String staticPath; //삭제

    public ChapStoryController(ChapStoryService chapStoryService) {this.chapStoryService=chapStoryService;}
    @GetMapping("/write.do")
    public String list() {
        return "chapstory/write"; // 해당 경로에 대한 뷰 이름 반환
    }

    @GetMapping("/list.do")
    public String chapList(
            Model model){
        List<ChapstorysDto> chaps;
        chaps= chapStoryService.list();
        model.addAttribute("chapstorys",chaps);
        return "chapstory/list";
    }

    @GetMapping("/{chapNum}/detail.do")
    public String chapDetail(
            Model model,
            @PathVariable int chapNum){
        ChapstorysDto chaps = chapStoryService.detail(chapNum);
        model.addAttribute("c",chaps);
        return "chapstory/detail";}

//    @GetMapping("/{userId}/blogMain.do")
//    public String chapMain(
//            Model model,
//            @PathVariable String userId,
//            @SessionAttribute UserDto loginUser){
//        List<ChapstorysDto> chaps;
//        chaps = chapStoryService.blogMain(userId);
//        model.addAttribute("chapstorys",chaps);
//        return "chapstory/blogMain";}

    @GetMapping("/{userId}/blogMain.do")
    public String chapMain(
            Model model,
            @PathVariable String userId,
            HttpSession session) {
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        List<ChapstorysDto> chaps;
        chaps = chapStoryService.blogMain(userId);
        model.addAttribute("chapstorys", chaps);
        return "chapstory/blogMain";
    }


    @GetMapping("/register.do")
    public void register(@SessionAttribute UserDto loginUser){}

    @PostMapping("/register.do")
    public String registerAction(
            RedirectAttributes redirectAttributes,
            @SessionAttribute UserDto loginUser,
            @ModelAttribute ChapstorysDto chaps,
            @RequestParam(name = "img", required = false)MultipartFile [] imgs) throws IOException {
        String redirectPage="redirect:/chapstory/register.do";
        if(!loginUser.getUserId().equals(chaps.getUserId())) return redirectPage;
        List<ChapstoryimgsDto> imgDtos=null;
        if(imgs!=null){
            imgDtos=new ArrayList<>();
            for(MultipartFile img : imgs){
                if(!img.isEmpty()){
                    String[] contentTypes=img.getContentType().split("/");
                    if(contentTypes[0].equals("image")){
                        String fileName=System.currentTimeMillis()+"_"+(int)(Math.random()*10000)+"."+contentTypes[1];
                        Path path = Paths.get(uploadPath+"/chapstory/"+fileName);
                        img.transferTo(path);
                        ChapstoryimgsDto imgDto = new ChapstoryimgsDto();
                        imgDto.setImg("/public/img/chapstory/"+fileName);
                        imgDtos.add(imgDto);
                    }
                }
            }
        }
        chaps.setChapstoryimgs(imgDtos);
        int register=0;
        String errorMsg=null;
        try{
            register=chapStoryService.register(chaps);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        try{
            if(register>0){
                redirectAttributes.addAttribute("chapNum", chaps.getChapNum());
                redirectPage="redirect:/chapstory/list.do";
            }else{
                //등록 실패시 저장했던 파일 삭제
                if(imgDtos!=null){
                    for(ChapstoryimgsDto i : imgDtos){
                        File imgFile=new File(staticPath+i.getImg());
                        if(imgFile.exists())imgFile.delete();
                    }
                }
            }
        }catch (Exception e){
            log.error(e);
            errorMsg=e.getMessage();
        }
        redirectAttributes.addFlashAttribute("msg","게시글 등록 실패 에러"+errorMsg);
        return redirectPage;
    }
}
