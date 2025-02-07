package com.soso_server.service;

import com.soso_server.utils.AES256;
import com.soso_server.dto.KakaoDTO;
import com.soso_server.dto.MemberDTO;
import com.soso_server.exception.MemberException;
import com.soso_server.ra.itf.MemberRAO;
import com.soso_server.service.itf.MemberService;
import com.soso_server.utils.ExternalAES256;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    MemberRAO rao;
    @Autowired
    AES256 aes256;
    @Autowired
    ExternalAES256 externalAES256;

    public void setRao(MemberRAO rao) {
        this.rao = rao;
    }

    @Override
    public String registerMember(String id){
        try{
            System.out.println("MemberServiceImpl.registerMember");
            if(id.length() < 20){
                throw new MemberException();
            }
            int decId = Integer.valueOf(aes256.decrypt(id));

            KakaoDTO kakaoDTO = rao.findKakaoByKakaoById(decId);

            if(rao.findMemberById(kakaoDTO.getId()) == null){
                System.out.println("신규 아이디 등록");
                MemberDTO memberDTO = new MemberDTO();
                memberDTO.setUserNickName(kakaoDTO.getKakaoNickName());
                memberDTO.setId(kakaoDTO.getId());
                rao.registerMember(memberDTO);
            }
            return aes256.encryptEncodeReplace(String.valueOf(rao.findMemberById(kakaoDTO.getId()).getUserId()));
        }catch (MemberException me){
            new MemberException("잘못된 id", -999);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public MemberDTO findMemberByUserId(String userId) throws MemberException {
        try{
            if(userId.length() < 20){
                throw new MemberException();
            }
//            MemberDTO memberDTO = rao.findMemberByUserId(Integer.parseInt(aes256.decrypt(URLDecoder.decode(userId.replaceAll("MSJSM", "%"), "UTF-8"))));
            MemberDTO memberDTO = rao.findMemberByUserId(Integer.parseInt(aes256.replaceDecodeDecryt(userId)));

            memberDTO.setId(0);
            memberDTO.setUserId(memberDTO.getUserId());
            return memberDTO;
        }catch (MemberException me){
            throw new MemberException("잘못된 userId", -999);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String modifyUserNickNameByUserId(String userId, String userNickName) throws MemberException {
        try{
            if(userNickName.length() > 30){
                throw new MemberException();
            }
            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setUserId(aes256.replaceDecodeDecryt(userId));
            memberDTO.setUserNickName(userNickName);
            rao.modifyUserNickNameByUserId(memberDTO);
            return userNickName;
        }catch (Exception e){
            throw new MemberException("너무 긴 userId", -999);
        }
    }

    @Override
    public int findMemberByLetterCount(String userId) throws MemberException {
        try{
            if(userId.length() < 20){
                throw new MemberException();
            }
//            return rao.findMemberByLetterCount(Integer.parseInt(aes256.decrypt(URLDecoder.decode(userId.replaceAll("MSJSM", "%"), "UTF-8"))));
            return rao.findMemberByLetterCount(Integer.parseInt(aes256.replaceDecodeDecryt(userId)));
        }catch (MemberException me){
            throw new MemberException("잘못된 userId", -999);
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Timestamp registerOpenDate(String userId) throws MemberException {
        try{
            if(userId.length() < 20){
                throw new MemberException();
            }
            Timestamp t = findOpenDate(userId);
            if(t != null && (new Timestamp(System.currentTimeMillis()-864000)).before(t)){
                throw new MemberException("이미 오픈데이트가 설정됨.", -999);
            }
            return rao.registerOpenDate(Integer.parseInt(aes256.replaceDecodeDecryt(userId)));
        }catch(MemberException e){
            throw new MemberException("잘못된 userId", -999);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Timestamp findOpenDate(String userId) throws MemberException {
        try{
            if(userId.length() < 20){
                throw new MemberException();
            }
            return rao.findOpenDate(Integer.parseInt(aes256.replaceDecodeDecryt(userId)));
        }catch(MemberException e){
            throw new MemberException("잘못된 userId", -999);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String changeExternalUserId(String userId) throws Exception {
        try {
            if(userId.length() < 20){
                throw new MemberException();
            }
//            return URLEncoder.encode(externalAES256.encrypt(aes256.decrypt(URLDecoder.decode(userId.replaceAll("MSJSM", "%"), "UTF-8"))), "UTF-8").replaceAll("%", "MSJSM");
            return externalAES256.encryptEncodeReplace(aes256.replaceDecodeDecryt(userId));

        }catch (MemberException me){
            throw new MemberException("잘못된 userId", -999);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public MemberDTO infoByExternalUserId(String userId) throws MemberException {
        try {
            if(userId.length() < 20){
                throw new MemberException();
            }

//            Integer decUserId = Integer.parseInt(externalAES256.decrypt(URLDecoder.decode(userId.replaceAll("MSJSM", "%"), "UTF-8")));
            Integer decUserId = Integer.parseInt(externalAES256.replaceDecodeDecryt(userId));

            MemberDTO memberDTO = rao.findMemberByUserId(decUserId);
            memberDTO.setId(0);
            memberDTO.setUserId("");
            memberDTO.setUserDate(null);
            return memberDTO;
        }catch (MemberException me){
            throw new MemberException("잘못된 userId", -999);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Integer findLetterCountByExternalUserId(String userId) {
        try{
            if(userId.length() < 20){
                throw new MemberException();
            }
            return rao.findMemberByLetterCount(Integer.parseInt(externalAES256.replaceDecodeDecryt(userId)));
        }catch (MemberException me){
//            throw new MemberException("잘못된 userId", -999);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MemberDTO> findMemberAll() {
        return rao.findMemberAll();
    }

}