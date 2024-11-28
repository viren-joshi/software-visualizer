package com.g8.props;

import lombok.Getter;
import lombok.Setter;

public class FileProps {

    // @Data annotation can not be set on this field
    @Setter
    @Getter
    private static String filePath;
}
