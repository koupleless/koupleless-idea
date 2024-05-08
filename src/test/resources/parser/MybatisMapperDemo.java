package com.example;

import com.mock.DO;
import org.apache.ibatis.annotations.Insert;

public interface DOMapper {
    @Insert({ "insert into mock_table (id)", "values (#{id,jdbcType=BIGINT})" })
    int insert(DO record);
}
