package com.acorn.chapspring.service;

import com.acorn.chapspring.dto.ChatMsgDto;
import com.acorn.chapspring.mapper.ChatMsgMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ChatMsgServiceImp implements ChatMsgService{
    private ChatMsgMapper chatMsgMapper;
    @Override
    public int register(ChatMsgDto chatMsgDto) {
        int register=chatMsgMapper.insertOne(chatMsgDto);
        return register;
    }

    @Override
    public List<ChatMsgDto> list(int crId) {
        List<ChatMsgDto> list=chatMsgMapper.findByCrId(crId);
        return null;
    }

    @Override
    public List<ChatMsgDto> list(int crId, String postTime) {
        List<ChatMsgDto> list=chatMsgMapper.findByCrIdAndGraterThanPostTime(crId,postTime);
        return list;
    }
}
