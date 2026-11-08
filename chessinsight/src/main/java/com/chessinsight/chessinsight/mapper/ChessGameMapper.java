package com.chessinsight.chessinsight.mapper;

import com.chessinsight.chessinsight.dto.ChessGameDto;
import com.chessinsight.chessinsight.game.ChessGame;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChessGameMapper {

    ChessGameDto toDto(ChessGame game);
}
