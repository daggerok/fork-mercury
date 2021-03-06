package com.github.ayberkcansever.mercury.io;

import com.github.ayberkcansever.mercury.Mercury;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SocketServer {

    private static final Logger LOG = LoggerFactory.getLogger(SocketServer.class);

    @PostConstruct
    public void init() {
        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()));
                                ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                                ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                                ch.pipeline().addLast(Mercury.getMercuryConfig().getClientClass().newInstance());
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_KEEPALIVE, true);

                int serverPort = Mercury.getMercuryConfig().getServerPort();
                ChannelFuture f = b.bind(serverPort).sync();
                LOG.info("Socket server started at port: " + serverPort);
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }).start();
    }

}
