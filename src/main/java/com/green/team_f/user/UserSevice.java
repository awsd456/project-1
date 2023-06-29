package com.green.team_f.user;

import com.green.team_f.user.model.UserEntity;
import com.green.team_f.user.model.UserInsDto;
import com.green.team_f.user.model.UserPatchPicDto;
import com.green.team_f.user.model.UserRemoveDto;
import com.green.team_f.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@Service
public class UserSevice {
    @Value("D:/download/F_Hpic")
    private String fileDir;
    private UserMapper mapper;


    @Autowired
    public UserSevice(UserMapper mapper){
        this.mapper = mapper;
    }

    public int insUser(UserInsDto dto){
        UserEntity entity = new UserEntity();
        //double bmr;
        entity.setUid(dto.getUid());
        entity.setUpw(dto.getUpw());
        entity.setName(dto.getName());
        entity.setAge(dto.getAge());
        entity.setHeight(dto.getHeight());
        entity.setWeight(dto.getWeight());
        //성별 대문자 변경
        char gender = Character.toUpperCase(dto.getGender());
        entity.setGender(gender);
        if (!(gender == 'M' || gender == 'F')) {
            return -1;
        }

        //기초대사량 측정
        if(entity.getGender() == 'M'){
          entity.setBmr(66.47 +(13.75 * dto.getHeight())+(5 * dto.getHeight())-(6.76 * dto.getAge()));
        } else {
          entity.setBmr(665.1+(9.56 * dto.getHeight())+(1.85 * dto.getHeight()) - (4.68 * dto.getAge()));
        }
        return mapper.insUser(entity);
    } // 회원등록 (사진제외 NULL값으로 날라감)

    public int updUserPic(MultipartFile pic, UserPatchPicDto dto){
        String centerPath = String.format("user/%d", dto.getIuser());
        String dicPath = String.format("%s/%s", fileDir, centerPath);

        File dic = new File(dicPath);           //폴더가 없을 경우 폴더를 생성
        if(!dic.exists()) {
            dic.mkdirs();
        }

        String originFileName = pic.getOriginalFilename();
        String savedFileName = FileUtil.makeRandomFileNm(originFileName);
        String savedFilePath = String.format("%s/%s", centerPath, savedFileName);
        String targetPath = String.format("%s/%s", fileDir, savedFilePath);
        File target = new File(targetPath);
        try {
            pic.transferTo(target);
        }catch (Exception e) {
            return 0;
        }
        dto.setUsepic(savedFilePath);
        try {
            int result = mapper.updUserPic(dto);
            if(result == 0) {
                throw new Exception("프로필 사진을 등록할 수 없습니다.");
            }
        } catch (Exception e) {
            //파일 삭제
            target.delete();
            return 0;
        }
        return 1;
    }

    public UserEntity selUser(UserEntity entity){
        return mapper.selUser(entity);
    }

    public int delUser(UserRemoveDto dto){
        String path = String.format("D:/download/F_Hpic/user/%d", dto.getIuser());
        //String dicPath = String.format("%s", fileDir);
        /*File dic = new File(path);           //폴더가 있을 경우 폴더를 삭제
        if(dic.exists()) {
            dic.delete();
        }*/

        File folder = new File(path);
       // folder.delete();
        try {
            while(folder.exists()) {
                File[] folder_list = folder.listFiles(); //파일리스트 얻어오기

                for (int j = 0; j < folder_list.length; j++) {
                    folder_list[j].delete(); //파일 삭제
                    System.out.println("파일이 삭제되었습니다.");

                }

                if(folder_list.length == 0 && folder.isDirectory()){
                    folder.delete(); //대상폴더 삭제
                    System.out.println("폴더가 삭제되었습니다.");
                }
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return mapper.delUser(dto);
    }




}
